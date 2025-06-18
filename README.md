# ♟️🧩 Color Sudoku N-Queens Puzzle Solver

This project captures an 8x8 colored Sudoku-like grid from a **webcam**, detects the color regions using **KMeans clustering**, and solves a variant of the **N-Queens puzzle** with additional constraints:
- One ♛ queen per row, column, and region.
- No ♛ queens can be adjacent, even diagonally.

✅ The final output is an interactive **HTML file** showing all valid solutions!

---

## 📁 Project Structure

N_Queens/  
&nbsp;&nbsp;&nbsp;│  
&nbsp;&nbsp;&nbsp;├── src/  
&nbsp;&nbsp;&nbsp;│  ├── ColorSudokuQueens.java # Java Solver  
&nbsp;&nbsp;&nbsp;│  └─ grid.py # Python Grid Detector  
&nbsp;&nbsp;&nbsp;├── lib/  
&nbsp;&nbsp;&nbsp;│  └── json-20210307.jar # JSON library required by Java  
&nbsp;&nbsp;&nbsp;├── color_sudoku_queens.html #solution visuals  
&nbsp;&nbsp;&nbsp;└── README.md # This file  

---

## 🧠 Features

- 🎥 Captures a live grid using your **webcam**.
- 🧮 Automatically detects grid size and clusters colors into regions.
- ♛ Solves the Sudoku Queen variant with Java.
- 🌐 Outputs colorful solutions in a user-friendly **HTML format**.

---

## 📤 Outputs
### 🐍Python output
- A 2D array  
  Parsed Grid:  
  [0, 0, 0, 0, 1, 1, 1, 1]  
  [0, 0, 0, 0, 0, 0, 0, 1]  
  [0, 4, 4, 0, 0, 6, 6, 1]  
  [4, 4, 0, 0, 6, 6, 1, 1]  
  [3, 3, 0, 0, 2, 2, 1, 1]  
  [3, 7, 3, 2, 5, 2, 1, 1]  
  [3, 7, 3, 2, 5, 2, 1, 1]  
  [3, 3, 3, 2, 2, 2, 1, 1]  

### ☕ Java output 
- A color_sudoku_queens.html file

---  

### 🎥 Images/Videos

https://github.com/user-attachments/assets/a6630c18-7e34-419b-bc59-78cfc0e020a9

---  

## 🛠 Requirements

### Python (for grid detection)
- Python 3.6+
- OpenCV (`cv2`)
- NumPy
- Matplotlib
- scikit-learn

Install using:

```bash
pip install opencv-python numpy matplotlib scikit-learn


