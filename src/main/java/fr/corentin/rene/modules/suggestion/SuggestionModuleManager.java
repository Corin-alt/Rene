package fr.corentin.rene.modules.suggestion;

import fr.corentin.rene.Rene;
import fr.corentin.rene.managers.CommandManager;
import fr.corentin.rene.moduleloading.parent.AModule;
import fr.corentin.rene.moduleloading.parent.AModuleConfiguration;
import fr.corentin.rene.modules.suggestion.commands.SuggestionChannelCommand;
import fr.corentin.rene.modules.suggestion.commands.SuggestionCommand;
import fr.corentin.rene.modules.suggestion.services.SuggestionDatabaseService;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SuggestionModuleManager extends AModule {

    private final Rene rene;
    private final SuggestionDatabaseService suggestionDatabaseService;
    private final Map<String, String> suggestionChannelCache;

    public SuggestionModuleManager() {
        this.rene = Rene.getInstance();
        this.suggestionDatabaseService = new SuggestionDatabaseService();
        this.suggestionChannelCache = new ConcurrentHashMap<>();
    }

    @Override
    public void registerCommands() {
        CommandManager.getInstance().registerPrefixedCommand(new SuggestionChannelCommand(suggestionDatabaseService));
        CommandManager.getInstance().registerSlashCommand(new SuggestionCommand(this));
    }

    @Override
    public void registerListeners() {
    }

    @Override
    public Class<? extends AModuleConfiguration> getConfigClass() {
        return SuggestionModuleConfiguration.class;
    }

    public String getSuggestionChannel(String guildId) {
        if (!suggestionChannelCache.containsKey(guildId)) {
            String channelId = suggestionDatabaseService.getSuggestionChannel(guildId);
            if (channelId == null || channelId.isEmpty()) {
                rene.getLogger().warn("Suggestion channel for guild {} not found, suggestions will be rejected", guildId);
                return channelId;
            }
            suggestionChannelCache.put(guildId, channelId);
        }
        return suggestionChannelCache.get(guildId);
    }

    public SuggestionDatabaseService getSuggestionDatabaseService() {
        return suggestionDatabaseService;
    }
}
