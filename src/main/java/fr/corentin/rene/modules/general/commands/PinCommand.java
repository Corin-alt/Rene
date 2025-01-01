package fr.corentin.rene.modules.general.commands;

import fr.corentin.rene.commands.Permission;
import fr.corentin.rene.commands.parent.AMessageInteractionCommand;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;

public class PinCommand extends AMessageInteractionCommand {

    public PinCommand() {
        super("Epingler", "Épingle le message ciblé", Permission.ALL, null);
    }

    @Override
    public boolean execute(GenericCommandInteractionEvent event) {
        MessageContextInteractionEvent event1 = (MessageContextInteractionEvent) event;

        event.deferReply().setEphemeral(false).queue(); // Acknowledge the command without sending a message
        event.getMessageChannel().retrievePinnedMessages().queue(pinnedMessages -> {
            if (pinnedMessages.stream().anyMatch(m -> m.getId().equals(event1.getTarget().getId()))) {
                event.getHook().sendMessage("Ce message est déjà épinglé.").setEphemeral(false).queue();
            } else {
                event.getMessageChannel()
                        .pinMessageById(event1.getTarget().getId())
                        .flatMap(v -> event.getHook().sendMessage("Message épinglé avec succès!").setEphemeral(false))
                        .queue(
                                success -> {
                                },
                                failure -> event.getHook().sendMessage("Erreur: Je ne peux pas épingler ce message!").setEphemeral(false).queue()
                        );
            }
        }, failure -> event.getHook().sendMessage("Erreur: Impossible de vérifier les messages épinglés.").setEphemeral(false).queue());

        return true;
    }
}
