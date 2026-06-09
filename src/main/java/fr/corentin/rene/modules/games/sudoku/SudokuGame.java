package fr.corentin.rene.modules.games.sudoku;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;

public class SudokuGame {
    private final String userId;
    private final String guildChannelId;
    private final LocalDate date;
    private final SudokuDifficulty difficulty;
    private final int[][] puzzle;
    private final int[][] solution;
    private final int[][] board;
    private final Instant startTime;
    private int moveCount;
    private long dmMessageId;
    private String lastFeedback;

    public SudokuGame(String userId, String guildChannelId, LocalDate date, SudokuDifficulty difficulty,
                      int[][] puzzle, int[][] solution) {
        this.userId = userId;
        this.guildChannelId = guildChannelId;
        this.date = date;
        this.difficulty = difficulty;
        this.puzzle = SudokuGenerator.copyGrid(puzzle);
        this.solution = SudokuGenerator.copyGrid(solution);
        this.board = SudokuGenerator.copyGrid(puzzle);
        this.startTime = Instant.now();
        this.moveCount = 0;
        this.lastFeedback = "Bonne chance !";
    }

    public boolean placeNumber(int row, int col, int value) {
        if (row < 0 || row >= 9 || col < 0 || col >= 9) return false;
        if (puzzle[row][col] != 0) return false;
        if (value < 1 || value > 9) return false;
        board[row][col] = value;
        moveCount++;
        return true;
    }

    public boolean eraseCell(int row, int col) {
        if (row < 0 || row >= 9 || col < 0 || col >= 9) return false;
        if (puzzle[row][col] != 0) return false;
        if (board[row][col] == 0) return false;
        board[row][col] = 0;
        moveCount++;
        return true;
    }

    public boolean isComplete() {
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                if (board[r][c] != solution[r][c]) return false;
            }
        }
        return true;
    }

    public int countErrors() {
        int errors = 0;
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                if (board[r][c] != 0 && puzzle[r][c] == 0 && board[r][c] != solution[r][c]) {
                    errors++;
                }
            }
        }
        return errors;
    }

    public int countRemaining() {
        int remaining = 0;
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                if (board[r][c] == 0) remaining++;
            }
        }
        return remaining;
    }

    public long getElapsedSeconds() {
        return Duration.between(startTime, Instant.now()).getSeconds();
    }

    public String getFormattedTime() {
        return formatTime(getElapsedSeconds());
    }

    public static String formatTime(long totalSeconds) {
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    public boolean isGivenCell(int row, int col) {
        return puzzle[row][col] != 0;
    }

    public String getUserId() {
        return userId;
    }

    public String getGuildChannelId() {
        return guildChannelId;
    }

    public LocalDate getDate() {
        return date;
    }

    public SudokuDifficulty getDifficulty() {
        return difficulty;
    }

    public int[][] getBoard() {
        return board;
    }

    public int[][] getPuzzle() {
        return puzzle;
    }

    public int[][] getSolution() {
        return solution;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public int getMoveCount() {
        return moveCount;
    }

    public long getDmMessageId() {
        return dmMessageId;
    }

    public void setDmMessageId(long dmMessageId) {
        this.dmMessageId = dmMessageId;
    }

    public String getLastFeedback() {
        return lastFeedback;
    }

    public void setLastFeedback(String lastFeedback) {
        this.lastFeedback = lastFeedback;
    }
}
