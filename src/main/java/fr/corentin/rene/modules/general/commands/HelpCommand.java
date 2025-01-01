package fr.corentin.rene.modules.general.commands;

import fr.corentin.rene.commands.parent.*;
import fr.corentin.rene.commands.parent.*;
import fr.corentin.rene.managers.CommandManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;

import java.awt.*;
import java.util.Map;

public class HelpCommand extends AInteractionCommand {

    public HelpCommand() {
        super("help", "Affiche l'ensemble des commandes disponibles", fr.corentin.rene.commands.Permission.ALL, null);
    }

    @Override
    public boolean execute(GenericCommandInteractionEvent event) {
        CommandManager commandManager = CommandManager.getInstance();
        Map<String, ACommand> commands = commandManager.getCommands();

        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("📚 Liste des Commandes");
        embedBuilder.setColor(Color.BLUE);
        embedBuilder.setDescription("Voici les commandes disponibles :");

        String interactionIcon = "🔹";
        String prefixedIcon = "🔸";
        String messageIcon = "📩";
        String userIcon = "👤";

        StringBuilder interactionCommands = new StringBuilder();
        StringBuilder messageInteractionCommands = new StringBuilder();
        StringBuilder userInteractionCommands = new StringBuilder();
        StringBuilder prefixedCommands = new StringBuilder();

        for (Map.Entry<String, ACommand> entry : commands.entrySet()) {
            ACommand command = entry.getValue();
            String commandEntry = String.format("%s **%s** - %s%n", interactionIcon, command.getName(), command.getDesc());

            if (command instanceof AMessageInteractionCommand) {
                messageInteractionCommands.append(commandEntry.replace(interactionIcon, messageIcon));
            } else if (command instanceof AUserInteractionCommand) {
                userInteractionCommands.append(commandEntry.replace(interactionIcon, userIcon));
            } else if (command instanceof AInteractionCommand) {
                interactionCommands.append(commandEntry);
            } else if (command instanceof APrefixedCommand && event.getMember().hasPermission(Permission.ADMINISTRATOR)) {
                prefixedCommands.append(commandEntry.replace(interactionIcon, prefixedIcon));
            }
        }

        if (!interactionCommands.isEmpty()) {
            embedBuilder.addField("**Commandes d'Interaction**", interactionCommands.toString(), false);
        }

        if (!messageInteractionCommands.isEmpty()) {
            embedBuilder.addField("**Commandes sur Message**", messageInteractionCommands.toString(), true);
        }

        if (!userInteractionCommands.isEmpty()) {
            embedBuilder.addField("**Commandes sur Utilisateur**", userInteractionCommands.toString(), true);
        }

        if (!prefixedCommands.isEmpty()) {
            embedBuilder.addField("Commandes Préfixées", prefixedCommands.toString(), false);
        }

        embedBuilder.setFooter("Rene | Help - Work In Progress", event.getJDA().getSelfUser().getAvatarUrl());
        event.replyEmbeds(embedBuilder.build()).setEphemeral(true).queue();

        return true;
    }
}
