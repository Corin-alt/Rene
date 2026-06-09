package fr.corentin.rene.modules.games.sudoku;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class SudokuGenerator {
    private static final int SIZE = 9;

    private final Random random;
    private int[][] solution;
    private int[][] puzzle;

    public SudokuGenerator(long seed) {
        this.random = new Random(seed);
    }

    public void generate(int cellsToRemove) {
        solution = new int[SIZE][SIZE];
        fillGrid(solution);
        puzzle = copyGrid(solution);
        removeCells(puzzle, cellsToRemove);
    }

    private boolean fillGrid(int[][] grid) {
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                if (grid[row][col] == 0) {
                    List<Integer> numbers = shuffledNumbers();
                    for (int num : numbers) {
                        if (isValid(grid, row, col, num)) {
                            grid[row][col] = num;
                            if (fillGrid(grid)) return true;
                            grid[row][col] = 0;
                        }
                    }
                    return false;
                }
            }
        }
        return true;
    }

    private List<Integer> shuffledNumbers() {
        List<Integer> numbers = new ArrayList<>();
        for (int i = 1; i <= SIZE; i++) numbers.add(i);
        Collections.shuffle(numbers, random);
        return numbers;
    }

    private void removeCells(int[][] grid, int count) {
        List<int[]> positions = new ArrayList<>();
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                positions.add(new int[]{r, c});
            }
        }
        Collections.shuffle(positions, random);
        for (int i = 0; i < count && i < positions.size(); i++) {
            grid[positions.get(i)[0]][positions.get(i)[1]] = 0;
        }
    }

    static boolean isValid(int[][] grid, int row, int col, int num) {
        for (int c = 0; c < SIZE; c++) {
            if (grid[row][c] == num) return false;
        }
        for (int r = 0; r < SIZE; r++) {
            if (grid[r][col] == num) return false;
        }
        int boxRow = (row / 3) * 3;
        int boxCol = (col / 3) * 3;
        for (int r = boxRow; r < boxRow + 3; r++) {
            for (int c = boxCol; c < boxCol + 3; c++) {
                if (grid[r][c] == num) return false;
            }
        }
        return true;
    }

    static int[][] copyGrid(int[][] grid) {
        int[][] copy = new int[SIZE][SIZE];
        for (int i = 0; i < SIZE; i++) {
            System.arraycopy(grid[i], 0, copy[i], 0, SIZE);
        }
        return copy;
    }

    public int[][] getSolution() {
        return solution;
    }

    public int[][] getPuzzle() {
        return puzzle;
    }
}
