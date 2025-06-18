package src;

import java.util.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.*;
import org.json.JSONArray;

public class ColorSudokuQueens {

    public boolean isSafe(char[][] board, int[][] color, Set<Integer> usedRegions, int row, int col) {
        int region = color[row][col];
        if (usedRegions.contains(region))
            return false;

        int n = board.length;
        int[] dx = { -1, -1, -1, 0, 0, 1, 1, 1 };
        int[] dy = { -1, 0, 1, -1, 1, -1, 0, 1 };

        for (int d = 0; d < 8; d++) {
            int ni = row + dx[d], nj = col + dy[d];
            if (ni >= 0 && nj >= 0 && ni < n && nj < n && board[ni][nj] == 'Q')
                return false;
        }

        for (int i = 0; i < n; i++)
            if (board[i][col] == 'Q')
                return false;

        for (int j = 0; j < n; j++)
            if (board[row][j] == 'Q')
                return false;

        return true;
    }

    public void saveBoard(char[][] board, List<List<String>> allBoards) {
        List<String> newBoard = new ArrayList<>();
        for (char[] row : board)
            newBoard.add(new String(row));
        allBoards.add(newBoard);
    }

    public void helper(char[][] board, int[][] color, Set<Integer> usedRegions,
            List<List<String>> allBoards, int row) {
        if (row == board.length) {
            if (usedRegions.size() == board.length)
                saveBoard(board, allBoards);
            return;
        }

        for (int col = 0; col < board.length; col++) {
            int region = color[row][col];
            if (isSafe(board, color, usedRegions, row, col)) {
                board[row][col] = 'Q';
                usedRegions.add(region);
                helper(board, color, usedRegions, allBoards, row + 1);
                board[row][col] = '.';
                usedRegions.remove(region);
            }
        }
    }

    public List<List<String>> solveColorSudokuQueens(int[][] color) {
        int n = color.length;
        List<List<String>> allBoards = new ArrayList<>();
        char[][] board = new char[n][n];
        for (char[] row : board)
            Arrays.fill(row, '.');
        Set<Integer> usedRegions = new HashSet<>();
        helper(board, color, usedRegions, allBoards, 0);
        return allBoards;
    }

    public void exportToHTML(List<List<String>> solutions, int[][] color, String filename) {
        try {
            String[] regionColors = {
                    "#ff9999", "#ffcc99", "#ffff99", "#ccff99", "#99ffcc", "#99ccff",
                    "#cc99ff", "#ff99cc", "#f0f0f0", "#d3d3d3", "#c0c0c0", "#a0a0a0",
                    "#ffb3ba", "#bae1ff", "#ffffba", "#baffc9", "#ffdfba", "#e0bbe4"
            };

            StringBuilder html = new StringBuilder();
            html.append("<!DOCTYPE html><html><head><meta charset=\"UTF-8\">");
            html.append("<title>Color Sudoku Queens</title><style>");
            html.append("body { font-family: Arial; margin: 20px; }");
            html.append("table { border-collapse: collapse; margin: 20px; display: inline-block; }");
            html.append(
                    "td { width: 40px; height: 40px; text-align: center; font-size: 24px; font-weight: bold; border: 2px solid #333; }");
            html.append("h3 { margin-top: 30px; }");
            html.append("</style></head><body>");

            html.append("<h1>Color Sudoku Queens Solutions</h1>");
            html.append("<p>Total Solutions Found: " + solutions.size() + "</p>");

            int count = 1;
            for (List<String> board : solutions) {
                html.append("<h3>Solution " + count++ + "</h3><table>");
                for (int i = 0; i < board.size(); i++) {
                    html.append("<tr>");
                    String row = board.get(i);
                    for (int j = 0; j < row.length(); j++) {
                        int region = color[i][j];
                        String bgColor = regionColors[region % regionColors.length];
                        String content = row.charAt(j) == 'Q' ? "♛" : "";
                        html.append("<td style='background-color:" + bgColor + "'>" + content + "</td>");
                    }
                    html.append("</tr>");
                }
                html.append("</table>");
            }

            html.append("</body></html>");
            Files.write(Paths.get(filename), html.toString().getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static int[][] getColorGridFromPython() throws IOException {
        ProcessBuilder pb = new ProcessBuilder("python", "src/grid.py"); // ✅ Adjust path if needed
        pb.redirectErrorStream(true); // ✅ Combine stderr with stdout
        Process process = pb.start();

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuilder jsonOutput = new StringBuilder();
        String line;

        // ✅ Collect all JSON lines (in case it's multi-line)
        while ((line = reader.readLine()) != null) {
            jsonOutput.append(line);
        }

        try {
            // ✅ Parse JSON into 2D array
            JSONArray outer = new JSONArray(jsonOutput.toString());
            int[][] result = new int[outer.length()][];
            for (int i = 0; i < outer.length(); i++) {
                JSONArray inner = outer.getJSONArray(i);
                result[i] = new int[inner.length()];
                for (int j = 0; j < inner.length(); j++) {
                    result[i][j] = inner.getInt(j);
                }
            }
            return result;
        } catch (Exception e) {
            System.err.println("Failed to parse Python JSON output:");
            e.printStackTrace();
            return new int[0][0];
        }
    }

    public static void main(String[] args) {
        try {
            int[][] color = getColorGridFromPython();

            // Test print
            System.out.println("Parsed Grid:");
            for (int[] row : color) {
                System.out.println(Arrays.toString(row));
            }

            ColorSudokuQueens solver = new ColorSudokuQueens();
            List<List<String>> solutions = solver.solveColorSudokuQueens(color);
            solver.exportToHTML(solutions, color, "color_sudoku_queens.html");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
