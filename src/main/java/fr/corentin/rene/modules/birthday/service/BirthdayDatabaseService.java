package fr.corentin.rene.modules.birthday.service;

import fr.corentin.rene.database.parent.ADatabaseService;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BirthdayDatabaseService extends ADatabaseService {
    public void createTable() {
        String sql = """
                CREATE TABLE IF NOT EXISTS birthdays (
                id integer PRIMARY KEY,
                user_id text NOT NULL UNIQUE,
                birthday date NOT NULL
                );""";
        dbManager.executeUpdate(sql, pstmt -> {});
    }

    public void insertBirthday(String userId, long birthday) {
        String sql = "INSERT INTO birthdays(user_id, birthday) VALUES(?,?) ON CONFLICT(user_id) DO UPDATE SET birthday = ?";
        dbManager.executeUpdate(sql, pstmt -> {
            try {
                pstmt.setString(1, userId);
                pstmt.setLong(2, birthday);
                pstmt.setLong(3, birthday);
            } catch (SQLException e) {
                logger.error("Failed to insert/update birthday", e);
            }
        });
    }

    public Map<String, Long> getTodayBirthdaysWithDates() {
        Map<String, Long> userBirthdays = new HashMap<>();
        LocalDate today = LocalDate.now();

        String sql = "SELECT user_id, birthday FROM birthdays WHERE strftime('%m-%d', datetime(birthday / 1000, 'unixepoch')) = strftime('%m-%d', ?);";

        List<Object[]> results = dbManager.executeQuery(sql,
                pstmt -> {
                    try {
                        pstmt.setString(1, today.toString());
                    } catch (SQLException e) {
                        logger.error("Failed to retrieve today's birthdays", e);
                    }
                },
                rs -> {
                    try {
                        return new Object[]{rs.getString("user_id"), rs.getLong("birthday")};
                    } catch (SQLException e) {
                        logger.error("Failed to read birthday result", e);
                    }
                    return null;
                });

        for (Object[] row : results) {
            if (row != null && row[0] != null && row[1] != null) {
                userBirthdays.put((String) row[0], (Long) row[1]);
            }
        }

        return userBirthdays;
    }

    public List<String> getTodayBirthdays() {
        return List.copyOf(getTodayBirthdaysWithDates().keySet());
    }
}
