package fr.corentin.rene.modules.games.sudoku;

public class SudokuRenderer {
    private static final char ESC = 27;
    private static final String ANSI_RESET = ESC + "[0m";
    private static final String ANSI_GIVEN = ESC + "[1;37m";
    private static final String ANSI_PLACED = ESC + "[1;36m";
    private static final String[] ROW_LABELS = {"A", "B", "C", "D", "E", "F", "G", "H", "I"};
    private static final String HEADER = "    1 2 3   4 5 6   7 8 9";
    private static final String TOP    = "  ┌───────┬───────┬───────┐";
    private static final String MID    = "  ├───────┼───────┼───────┤";
    private static final String BOTTOM = "  └───────┴───────┴───────┘";

    public static String render(SudokuGame game) {
        int[][] board = game.getBoard();

        StringBuilder sb = new StringBuilder();
        sb.append("```ansi\n");
        sb.append(HEADER).append('\n');
        sb.append(TOP).append('\n');

        for (int row = 0; row < 9; row++) {
            if (row == 3 || row == 6) {
                sb.append(MID).append('\n');
            }

            sb.append(ROW_LABELS[row]);
            for (int col = 0; col < 9; col++) {
                if (col % 3 == 0) {
                    sb.append(" │");
                }
                sb.append(' ');
                renderCell(sb, board[row][col], game.isGivenCell(row, col));
            }
            sb.append(" │\n");
        }

        sb.append(BOTTOM).append('\n');
        sb.append("```");

        return sb.toString();
    }

    public static String renderSolution(int[][] solution) {
        StringBuilder sb = new StringBuilder();
        sb.append("```ansi\n");
        sb.append(HEADER).append('\n');
        sb.append(TOP).append('\n');

        for (int row = 0; row < 9; row++) {
            if (row == 3 || row == 6) {
                sb.append(MID).append('\n');
            }

            sb.append(ROW_LABELS[row]);
            for (int col = 0; col < 9; col++) {
                if (col % 3 == 0) {
                    sb.append(" │");
                }
                sb.append(' ');
                sb.append(ANSI_GIVEN).append(solution[row][col]).append(ANSI_RESET);
            }
            sb.append(" │\n");
        }

        sb.append(BOTTOM).append('\n');
        sb.append("```");

        return sb.toString();
    }

    private static void renderCell(StringBuilder sb, int value, boolean given) {
        if (value == 0) {
            sb.append('·');
        } else if (given) {
            sb.append(ANSI_GIVEN).append(value).append(ANSI_RESET);
        } else {
            sb.append(ANSI_PLACED).append(value).append(ANSI_RESET);
        }
    }
}
