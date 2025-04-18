package fr.corentin.rene.events;

import fr.corentin.rene.Rene;
import fr.corentin.rene.events.parent.AEventListener;
import fr.corentin.rene.managers.CommandManager;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class PrefixedCommandsListener extends AEventListener {

    private final Rene rene;
    private final CommandManager commandManager;

    public PrefixedCommandsListener() {
        this.commandManager = CommandManager.getInstance();
        this.rene = Rene.getInstance();
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        String guildId = event.getGuild().getId();
        String prefix = rene.getCommandPrefix(guildId);

        String content = event.getMessage().getContentRaw();
        if (!content.startsWith(prefix)) {
            return;
        }

        String[] parts = content.substring(prefix.length()).split("\\s+");

        if (parts[0].matches(String.format("\\%s+", prefix))) {
            return;
        }

        String commandName = parts[0].toLowerCase();
        String[] args = parts.length > 1 ? Arrays.copyOfRange(parts, 1, parts.length) : new String[0];
        commandManager.handlePrefixedCommand(event, commandName, args);
    }
}
