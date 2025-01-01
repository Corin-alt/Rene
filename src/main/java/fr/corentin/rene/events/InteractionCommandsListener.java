package fr.corentin.rene.events;

import fr.corentin.rene.events.parent.AEventListener;
import fr.corentin.rene.managers.CommandManager;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import org.jetbrains.annotations.NotNull;

public class InteractionCommandsListener extends AEventListener {
    private final CommandManager commandManager;

    public InteractionCommandsListener() {
        this.commandManager = CommandManager.getInstance();
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        commandManager.handleCommand(event);
    }

    @Override
    public void onUserContextInteraction(@NotNull UserContextInteractionEvent event) {
        commandManager.handleCommand(event);
    }

    @Override
    public void onMessageContextInteraction(@NotNull MessageContextInteractionEvent event) {
        commandManager.handleCommand(event);
    }
}
