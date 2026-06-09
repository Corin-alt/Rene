package fr.corentin.rene.modules.games.sudoku;

import java.time.LocalDate;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SudokuGameManager {
    private final Map<String, SudokuGame> activeGames = new ConcurrentHashMap<>();

    private LocalDate cachedDate;
    private final Map<SudokuDifficulty, int[][]> cachedPuzzles = new EnumMap<>(SudokuDifficulty.class);
    private final Map<SudokuDifficulty, int[][]> cachedSolutions = new EnumMap<>(SudokuDifficulty.class);

    public synchronized void ensureDailyPuzzles() {
        LocalDate today = LocalDate.now();
        if (cachedDate != null && today.equals(cachedDate)) return;

        cachedPuzzles.clear();
        cachedSolutions.clear();
        long baseSeed = today.toEpochDay();

        for (SudokuDifficulty diff : SudokuDifficulty.values()) {
            SudokuGenerator generator = new SudokuGenerator(baseSeed + diff.ordinal() * 1000L);
            generator.generate(diff.getCellsToRemove());
            cachedPuzzles.put(diff, generator.getPuzzle());
            cachedSolutions.put(diff, generator.getSolution());
        }

        cachedDate = today;
    }

    public SudokuGame startGame(String userId, String channelId, SudokuDifficulty difficulty) {
        ensureDailyPuzzles();
        SudokuGame game = new SudokuGame(
                userId, channelId, cachedDate, difficulty,
                cachedPuzzles.get(difficulty), cachedSolutions.get(difficulty)
        );
        activeGames.put(userId, game);
        return game;
    }

    public SudokuGame getGame(String userId) {
        return activeGames.get(userId);
    }

    public void removeGame(String userId) {
        activeGames.remove(userId);
    }

    public boolean hasActiveGame(String userId) {
        return activeGames.containsKey(userId);
    }
}
