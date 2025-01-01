package fr.corentin.rene.modules.birthday.service;

import fr.corentin.rene.database.parent.ADatabaseService;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class BirthdayDatabaseService extends ADatabaseService {
    public void createTable() {
        String sql = """
                CREATE TABLE IF NOT EXISTS birthdays (
                id integer PRIMARY KEY,
                user_id text NOT NULL UNIQUE,
                birthday date NOT NULL
                );""";
        dbManager.executeUpdate(sql, pstmt -> {
        });
    }

    public void insertBirthday(String userId, long birthday) {
        String sql = "INSERT INTO birthdays(user_id, birthday) VALUES(?,?) ON CONFLICT(user_id) DO UPDATE SET birthday = ?";
        dbManager.executeUpdate(sql, pstmt -> {
            try {
                pstmt.setString(1, userId);
                pstmt.setLong(2, birthday);
                pstmt.setLong(3, birthday);
            } catch (SQLException e) {
                logger.error("Failed to insert/update birthday: " + e.getMessage(), e);
            }
        });
    }

    public List<String> getTodayBirthdays() {
        List<String> userIds;
        LocalDate today = LocalDate.now();

        //String sql = "SELECT user_id FROM birthdays WHERE strftime('%d/%m', birthday) = ?";
        String sql = "SELECT user_id FROM birthdays WHERE strftime('%m-%d', datetime(birthday / 1000, 'unixepoch')) = strftime('%m-%d', ?);";

        userIds = dbManager.executeQuery(sql,
                pstmt -> {
                    try {
                        pstmt.setString(1, today.toString());
                    } catch (SQLException e) {
                        logger.error("Failed to retrieve today's birthdays: " + e.getMessage(), e);
                    }
                },
                rs -> {
                    try {
                        return rs.getString("user_id");
                    } catch (SQLException e) {
                        logger.error("Failed to retrieve today's birthdays: " + e.getMessage(), e);
                    }
                    return null;
                });
        return userIds;
    }
}
