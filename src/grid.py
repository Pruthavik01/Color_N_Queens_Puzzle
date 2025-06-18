
# ---------------------- Imports ----------------------
import cv2
import numpy as np
import matplotlib.pyplot as plt
from sklearn.cluster import KMeans
import json
import sys

# ---------------------- Utility Functions ----------------------
def order_points(pts):
    rect = np.zeros((4, 2), dtype="float32")
    s = pts.sum(axis=1)
    diff = np.diff(pts, axis=1)
    rect[0] = pts[np.argmin(s)]    # top-left
    rect[2] = pts[np.argmax(s)]    # bottom-right
    rect[1] = pts[np.argmin(diff)] # top-right
    rect[3] = pts[np.argmax(diff)] # bottom-left
    return rect

def four_point_transform(image, pts):
    rect = order_points(pts)
    (tl, tr, br, bl) = rect

    widthA = np.linalg.norm(br - bl)
    widthB = np.linalg.norm(tr - tl)
    heightA = np.linalg.norm(tr - br)
    heightB = np.linalg.norm(tl - bl)

    maxWidth = int(max(widthA, widthB))
    maxHeight = int(max(heightA, heightB))

    dst = np.array([
        [0, 0],
        [maxWidth - 1, 0],
        [maxWidth - 1, maxHeight - 1],
        [0, maxHeight - 1]], dtype="float32")

    M = cv2.getPerspectiveTransform(rect, dst)
    warped = cv2.warpPerspective(image, M, (maxWidth, maxHeight))
    return warped

def find_largest_square_contour(gray_img):
    blur = cv2.GaussianBlur(gray_img, (5, 5), 0)
    edged = cv2.Canny(blur, 50, 150)
    contours, _ = cv2.findContours(edged, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)

    max_area = 0
    best_approx = None

    for cnt in contours:
        peri = cv2.arcLength(cnt, True)
        approx = cv2.approxPolyDP(cnt, 0.02 * peri, True)
        if len(approx) == 4 and cv2.isContourConvex(approx):
            area = cv2.contourArea(approx)
            if area > max_area:
                max_area = area
                best_approx = approx

    return best_approx.reshape(4, 2) if best_approx is not None else None

def detect_grid_size(warped_img):
    gray = cv2.cvtColor(warped_img, cv2.COLOR_RGB2GRAY)
    edges = cv2.Canny(gray, 50, 150, apertureSize=3)
    lines = cv2.HoughLinesP(edges, 1, np.pi / 180, threshold=100, minLineLength=30, maxLineGap=10)

    h_lines = []
    v_lines = []

    if lines is not None:
        for line in lines:
            x1, y1, x2, y2 = line[0]
            if abs(y2 - y1) < 10:  # Horizontal
                h_lines.append((y1 + y2) // 2)
            elif abs(x2 - x1) < 10:  # Vertical
                v_lines.append((x1 + x2) // 2)

    def deduplicate(lines, min_gap=10):
        lines = sorted(lines)
        deduped = []
        for l in lines:
            if not deduped or abs(l - deduped[-1]) > min_gap:
                deduped.append(l)
        return deduped

    h_lines = deduplicate(h_lines)
    v_lines = deduplicate(v_lines)

    line_count = max(len(h_lines), len(v_lines))
    grid_size = line_count - 1 if line_count > 1 else 8  # fallback to 8x8
    return grid_size, grid_size


# ---------------------- Capture and Process Loop ----------------------
cap = cv2.VideoCapture(0)
img64 = None  # default placeholder

while True:
    ret, frame = cap.read()
    if not ret:
        break

    key = cv2.waitKey(1) & 0xFF
    cv2.imshow("Live Camera", frame)

    if key == ord(' '):  # Press SPACE to capture
        gray = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)
        corners = find_largest_square_contour(gray)

        if corners is not None:
            warped = four_point_transform(frame, corners)
            img64 = cv2.cvtColor(warped, cv2.COLOR_BGR2RGB)
            break  # Successfully captured
    elif key == ord('q'):  # Press Q to quit
        break

cap.release()
cv2.destroyAllWindows()

# ---------------------- Validate ----------------------
if img64 is None:
    sys.stderr.write("ERROR: No grid was captured.\n")
    sys.exit(1)

# ---------------------- Auto Grid Size Detection ----------------------
rows, cols = detect_grid_size(img64)

# ---------------------- Color Region Extraction ----------------------
height, width = img64.shape[:2]
cell_h, cell_w = height // rows, width // cols
cell_colors = []

for i in range(rows):
    row = []
    for j in range(cols):
        y1, y2 = i * cell_h + 4, (i + 1) * cell_h - 4
        x1, x2 = j * cell_w + 4, (j + 1) * cell_w - 4
        cell = img64[y1:y2, x1:x2]
        avg_color = np.mean(cell.reshape(-1, 3), axis=0)
        row.append(avg_color)
    cell_colors.append(row)

# ---------------------- KMeans Clustering ----------------------
flat_colors = np.array(cell_colors).reshape(-1, 3)
kmeans = KMeans(n_clusters=8, random_state=0, n_init=20).fit(flat_colors)
labels = kmeans.labels_.reshape(rows, cols)

# ---------------------- JSON Output ----------------------
region_grid = labels.tolist()
print(json.dumps(region_grid))  # This is the only output!
