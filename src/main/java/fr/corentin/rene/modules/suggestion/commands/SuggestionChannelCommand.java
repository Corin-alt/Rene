package fr.corentin.rene.modules.suggestion.commands;

import fr.corentin.rene.commands.Permission;
import fr.corentin.rene.commands.parent.APrefixedCommand;
import fr.corentin.rene.modules.suggestion.services.SuggestionDatabaseService;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class SuggestionChannelCommand extends APrefixedCommand {

    private final SuggestionDatabaseService suggestionDatabaseService;

    public SuggestionChannelCommand(SuggestionDatabaseService suggestionDatabaseService) {
        super("suggestion-channel", "Défini le channel dans lequel sont envoyés les suggestions", Permission.ADMIN);
        this.suggestionDatabaseService = suggestionDatabaseService;
    }

    @Override
    public boolean execute(MessageReceivedEvent event, String[] args) {
        String channelId = event.getChannel().getId();
        event.getMessage().delete().queue();

        suggestionDatabaseService.setSuggestionChannel(event.getGuild().getId(), channelId);

        return true;
    }
}
