package fr.corentin.rene.managers;

import fr.corentin.rene.Rene;
import org.slf4j.Logger;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class DatabaseManager {
    private static final String DB_FILENAME_PREFIX = "jdbc:sqlite:";
    private static final String DB_FILENAME = "rene.db";
    private static DatabaseManager databaseManager;

    private final Logger logger;
    private final String dbFile;

    public DatabaseManager() {
        Rene rene = Rene.getInstance();
        this.logger = rene.getLogger();
        this.dbFile = DB_FILENAME_PREFIX + rene.getDataFolder().getPath() + File.separator + DB_FILENAME;
        try {
            createNewDatabase();
            createPrefixTable();
        } catch (SQLException e) {
            logger.error("Database error", e);
        }
    }

    private void createNewDatabase() throws SQLException {
        try (Connection conn = DriverManager.getConnection(dbFile)) {
            if (conn != null) {
                logger.info("A new database has been created or existing database connected.");
            }
        }
    }

    private void createPrefixTable() {
        String sql = """
            CREATE TABLE IF NOT EXISTS command_prefixes (
                guild_id TEXT PRIMARY KEY,
                prefix TEXT NOT NULL
            );""";

        executeUpdate(sql, pstmt -> {});
    }

    public String getCommandPrefix(String guildId) {
        String sql = "SELECT prefix FROM command_prefixes WHERE guild_id = ?";
        List<String> result = executeQuery(sql, pstmt -> {

            try {
                pstmt.setString(1, guildId);
            } catch (SQLException e) {
                logger.error("Database get prefix error: " + e.getMessage(), e);
            }
        }, rs -> {
            try {
                return rs.getString("prefix");
            } catch (SQLException e) {
                logger.error("Database get prefix error: " + e.getMessage(), e);
                return null;
            }
        });

        return result.isEmpty() ? null : result.get(0);
    }

    public void setCommandPrefix(String guildId, String prefix) {
        String sql = "INSERT INTO command_prefixes (guild_id, prefix) VALUES (?, ?) ON CONFLICT(guild_id) DO UPDATE SET prefix = ?";
        executeUpdate(sql, pstmt -> {
            try {
                pstmt.setString(1, guildId);
                pstmt.setString(2, prefix);
                pstmt.setString(3, prefix);
            } catch (SQLException e) {
                logger.error("Database prefix update error: " + e.getMessage(), e);
            }
        });
    }

    // Generic method to execute update queries
    public void executeUpdate(String sql, Consumer<PreparedStatement> parameterSetter) {
        try (Connection conn = DriverManager.getConnection(dbFile);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            parameterSetter.accept(pstmt);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Database update error: " + e.getMessage(), e);
        }
    }

    // Generic method to execute a select query
    public <T> List<T> executeQuery(String sql, Consumer<PreparedStatement> parameterSetter, Function<ResultSet, T> resultProcessor) {
        List<T> results = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(dbFile);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            parameterSetter.accept(pstmt);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    results.add(resultProcessor.apply(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Database query error: " + e.getMessage(), e);
        }
        return results;
    }

    public int executeQueryForInt(String sql, Consumer<PreparedStatement> parameterSetter) {
        int result = 0;
        try (Connection conn = DriverManager.getConnection(dbFile);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            parameterSetter.accept(pstmt);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    result = rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            logger.error("Database query error: " + e.getMessage(), e);
        }
        return result;
    }

    public static synchronized DatabaseManager getInstance() {
        if (databaseManager == null) {
            databaseManager = new DatabaseManager();
        }
        return databaseManager;
    }
}
