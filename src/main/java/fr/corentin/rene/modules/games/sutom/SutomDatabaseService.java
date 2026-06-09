package fr.corentin.rene.modules.games.sutom;

import fr.corentin.rene.database.parent.ADatabaseService;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class SutomDatabaseService extends ADatabaseService {

    @Override
    protected void createTable() {
        String sql = """
                CREATE TABLE IF NOT EXISTS sutom_scores (
                    user_id TEXT NOT NULL,
                    date TEXT NOT NULL,
                    attempts INTEGER NOT NULL,
                    time_seconds INTEGER NOT NULL,
                    completed_at INTEGER NOT NULL,
                    PRIMARY KEY (user_id, date)
                );""";
        dbManager.executeUpdate(sql, pstmt -> {});
    }

    public void insertScore(String userId, LocalDate date, int attempts, long timeSeconds) {
        String sql = "INSERT OR IGNORE INTO sutom_scores (user_id, date, attempts, time_seconds, completed_at) VALUES (?, ?, ?, ?, ?)";
        dbManager.executeUpdate(sql, pstmt -> {
            try {
                pstmt.setString(1, userId);
                pstmt.setString(2, date.toString());
                pstmt.setInt(3, attempts);
                pstmt.setLong(4, timeSeconds);
                pstmt.setLong(5, System.currentTimeMillis());
            } catch (SQLException e) {
                logger.error("Failed to insert sutom score", e);
            }
        });
    }

    public boolean hasCompletedToday(String userId, LocalDate date) {
        String sql = "SELECT COUNT(*) FROM sutom_scores WHERE user_id = ? AND date = ?";
        return dbManager.executeQueryForInt(sql, pstmt -> {
            try {
                pstmt.setString(1, userId);
                pstmt.setString(2, date.toString());
            } catch (SQLException e) {
                logger.error("Failed to check sutom completion", e);
            }
        }) > 0;
    }

    public List<ScoreEntry> getDailyScores(LocalDate date) {
        String sql = "SELECT user_id, attempts, time_seconds FROM sutom_scores WHERE date = ? ORDER BY attempts ASC, time_seconds ASC";
        return dbManager.executeQuery(sql, pstmt -> {
            try {
                pstmt.setString(1, date.toString());
            } catch (SQLException e) {
                logger.error("Failed to get daily sutom scores", e);
            }
        }, rs -> {
            try {
                return new ScoreEntry(
                        rs.getString("user_id"),
                        rs.getInt("attempts"),
                        rs.getLong("time_seconds")
                );
            } catch (SQLException e) {
                logger.error("Failed to read sutom score", e);
                return null;
            }
        });
    }

    public List<AllTimeEntry> getAllTimeScores() {
        String sql = """
                SELECT user_id,
                       COUNT(*) as games_played,
                       CAST(AVG(attempts) AS REAL) as avg_attempts,
                       MIN(attempts) as best_attempts,
                       CAST(AVG(time_seconds) AS INTEGER) as avg_time,
                       MIN(time_seconds) as best_time
                FROM sutom_scores
                GROUP BY user_id
                ORDER BY avg_attempts ASC, avg_time ASC""";
        return dbManager.executeQuery(sql, pstmt -> {}, rs -> {
            try {
                return new AllTimeEntry(
                        rs.getString("user_id"),
                        rs.getInt("games_played"),
                        rs.getDouble("avg_attempts"),
                        rs.getInt("best_attempts"),
                        rs.getLong("avg_time"),
                        rs.getLong("best_time")
                );
            } catch (SQLException e) {
                logger.error("Failed to read all-time sutom score", e);
                return null;
            }
        });
    }

    public record ScoreEntry(String userId, int attempts, long timeSeconds) {}
    public record AllTimeEntry(String userId, int gamesPlayed, double avgAttempts, int bestAttempts,
                               long avgTimeSeconds, long bestTimeSeconds) {}
}
