package fr.corentin.rene.modules.games.sudoku;

public enum SudokuDifficulty {
    EASY("Facile", 35, "🟢"),
    MEDIUM("Moyen", 45, "🟡"),
    HARD("Difficile", 55, "🔴");

    private final String label;
    private final int cellsToRemove;
    private final String emoji;

    SudokuDifficulty(String label, int cellsToRemove, String emoji) {
        this.label = label;
        this.cellsToRemove = cellsToRemove;
        this.emoji = emoji;
    }

    public String getLabel() {
        return label;
    }

    public int getCellsToRemove() {
        return cellsToRemove;
    }

    public String getEmoji() {
        return emoji;
    }
}
