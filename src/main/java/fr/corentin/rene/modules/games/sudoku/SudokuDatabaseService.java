package fr.corentin.rene.modules.games.sudoku;

import fr.corentin.rene.database.parent.ADatabaseService;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class SudokuDatabaseService extends ADatabaseService {

    @Override
    protected void createTable() {
        String sql = """
                CREATE TABLE IF NOT EXISTS sudoku_scores (
                    user_id TEXT NOT NULL,
                    date TEXT NOT NULL,
                    difficulty TEXT NOT NULL,
                    time_seconds INTEGER NOT NULL,
                    move_count INTEGER NOT NULL,
                    completed_at INTEGER NOT NULL,
                    PRIMARY KEY (user_id, date, difficulty)
                );""";
        dbManager.executeUpdate(sql, pstmt -> {});
    }

    public void insertScore(String userId, LocalDate date, SudokuDifficulty difficulty, long timeSeconds, int moveCount) {
        String sql = "INSERT OR IGNORE INTO sudoku_scores (user_id, date, difficulty, time_seconds, move_count, completed_at) VALUES (?, ?, ?, ?, ?, ?)";
        dbManager.executeUpdate(sql, pstmt -> {
            try {
                pstmt.setString(1, userId);
                pstmt.setString(2, date.toString());
                pstmt.setString(3, difficulty.name());
                pstmt.setLong(4, timeSeconds);
                pstmt.setInt(5, moveCount);
                pstmt.setLong(6, System.currentTimeMillis());
            } catch (SQLException e) {
                logger.error("Failed to insert sudoku score", e);
            }
        });
    }

    public boolean hasCompletedToday(String userId, LocalDate date, SudokuDifficulty difficulty) {
        String sql = "SELECT COUNT(*) FROM sudoku_scores WHERE user_id = ? AND date = ? AND difficulty = ?";
        return dbManager.executeQueryForInt(sql, pstmt -> {
            try {
                pstmt.setString(1, userId);
                pstmt.setString(2, date.toString());
                pstmt.setString(3, difficulty.name());
            } catch (SQLException e) {
                logger.error("Failed to check sudoku completion", e);
            }
        }) > 0;
    }

    public List<ScoreEntry> getDailyScores(LocalDate date, SudokuDifficulty difficulty) {
        String sql = "SELECT user_id, time_seconds, move_count FROM sudoku_scores WHERE date = ? AND difficulty = ? ORDER BY time_seconds ASC";
        return dbManager.executeQuery(sql, pstmt -> {
            try {
                pstmt.setString(1, date.toString());
                pstmt.setString(2, difficulty.name());
            } catch (SQLException e) {
                logger.error("Failed to get daily sudoku scores", e);
            }
        }, rs -> {
            try {
                return new ScoreEntry(
                        rs.getString("user_id"),
                        rs.getLong("time_seconds"),
                        rs.getInt("move_count")
                );
            } catch (SQLException e) {
                logger.error("Failed to read sudoku score", e);
                return null;
            }
        });
    }

    public List<AllTimeEntry> getAllTimeScores(SudokuDifficulty difficulty) {
        String sql = """
                SELECT user_id,
                       COUNT(*) as games_played,
                       CAST(AVG(time_seconds) AS INTEGER) as avg_time,
                       MIN(time_seconds) as best_time
                FROM sudoku_scores
                WHERE difficulty = ?
                GROUP BY user_id
                ORDER BY avg_time ASC""";
        return dbManager.executeQuery(sql, pstmt -> {
            try {
                pstmt.setString(1, difficulty.name());
            } catch (SQLException e) {
                logger.error("Failed to get all-time sudoku scores", e);
            }
        }, rs -> {
            try {
                return new AllTimeEntry(
                        rs.getString("user_id"),
                        rs.getInt("games_played"),
                        rs.getLong("avg_time"),
                        rs.getLong("best_time")
                );
            } catch (SQLException e) {
                logger.error("Failed to read all-time sudoku score", e);
                return null;
            }
        });
    }

    public record ScoreEntry(String userId, long timeSeconds, int moveCount) {}

    public record AllTimeEntry(String userId, int gamesPlayed, long avgTimeSeconds, long bestTimeSeconds) {}
}
