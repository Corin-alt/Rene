package fr.corentin.rene.modules.suggestion.services;

import fr.corentin.rene.Rene;
import fr.corentin.rene.managers.DatabaseManager;
import org.slf4j.Logger;

import java.sql.SQLException;
import java.util.List;

public class SuggestionDatabaseService {
    private final DatabaseManager dbManager;
    private final Logger logger;

    public SuggestionDatabaseService() {
        this.dbManager = DatabaseManager.getInstance();
        this.logger = Rene.getInstance().getLogger();
        createTable();
    }

    private void createTable() {
        String sql = """
                CREATE TABLE IF NOT EXISTS suggestion_settings (
                     guild_id TEXT PRIMARY KEY,
                     channel_id TEXT
                 );""";
        dbManager.executeUpdate(sql, pstmt -> {
        });
    }

    public void setSuggestionChannel(String guildId, String channelId) {
        String sql = "INSERT INTO suggestion_settings (guild_id, channel_id) VALUES (?, ?) ON CONFLICT(guild_id) DO UPDATE SET channel_id = ?";
        dbManager.executeUpdate(sql, pstmt -> {
            try {
                pstmt.setString(1, guildId);
                pstmt.setString(2, channelId);
                pstmt.setString(3, channelId);
            } catch (SQLException e) {
                logger.error("Failed to insert/update suggestion channel for guildId {}", guildId);
            }
        });
        logger.info("Suggestion channel defined to {} for guildId {}", channelId, guildId);
    }

    public String getSuggestionChannel(String guildId) {
        String sql = "SELECT channel_id FROM suggestion_settings WHERE guild_id = ?";
        List<String> result = dbManager.executeQuery(sql, pstmt -> {
            try {
                pstmt.setString(1, guildId);
            } catch (SQLException e) {
                logger.error("Failed to retrieve suggestion channel for guildId {}: ", guildId);
            }
        }, rs -> {
            try {
                return rs.getString("channel_id");
            } catch (SQLException e) {
                logger.error("Failed to retrieve suggestion channel for guildId {}: ", guildId);
            }
            return null;
        });
        return result.isEmpty() ? null : result.get(0);
    }
}
