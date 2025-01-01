package fr.corentin.rene.managers;

import fr.corentin.rene.Rene;
import fr.corentin.rene.commands.Permission;
import fr.corentin.rene.commands.parent.ACommand;
import fr.corentin.rene.commands.parent.AInteractionCommand;
import fr.corentin.rene.commands.parent.APrefixedCommand;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.util.HashMap;
import java.util.Map;

public class CommandManager {
    private static CommandManager commandManager;

    private final JDA jda;
    private final Map<String, ACommand> commands;

    public CommandManager(JDA jda) {
        this.jda = jda;

        commands = new HashMap<>();
    }

    public void registerPrefixedCommand(APrefixedCommand command) {
        registerCommand(command, null);
    }

    public void registerSlashCommand(AInteractionCommand command) {
        SlashCommandData slashCommandData = Commands.slash(command.getName(), command.getDesc());

        if (command.getOptions() != null) {
            slashCommandData.addOptions(command.getOptions());
        }

        registerCommand(command, slashCommandData);
    }

    public void registerMessageContextCommand(AInteractionCommand aCommand) {
        CommandData contextCommandData = Commands.message(aCommand.getName());
        registerCommand(aCommand, contextCommandData);
    }

    public void registerUserContextCommand(AInteractionCommand aCommand) {
        CommandData contextCommandData = Commands.user(aCommand.getName());
        registerCommand(aCommand, contextCommandData);
    }

    private void registerCommand(ACommand aCommand, CommandData commandData) {
        commands.put(aCommand.getName(), aCommand);

        // In case of prefixed commandData, nothing to register in jda commandData manager
        if (commandData == null) {
            return;
        }

        if (aCommand.getPermission() == Permission.ALL) {
            commandData.setDefaultPermissions(DefaultMemberPermissions.ENABLED);
        } else {
            commandData.setDefaultPermissions(DefaultMemberPermissions.DISABLED);
        }

        jda.getGuilds().get(0).upsertCommand(commandData).queue();
    }

    public void unregisterCommand(ACommand command) {
        commands.remove(command.getName());
    }

    public void handleCommand(GenericCommandInteractionEvent event) {
        String commandName = event.getName();
        AInteractionCommand command = (AInteractionCommand) commands.get(commandName);

        if (command == null) {
            event.reply("Commande non reconnue !").queue();
            return;
        }

        if (command.hasPermission(event.getMember())) {
            event.reply("Tu n'as pas la permission d'utiliser cette commande !").setEphemeral(true).queue();
        } else {
            command.execute(event);
        }
    }

    public void handlePrefixedCommand(MessageReceivedEvent event, String commandName, String[] args) {
        APrefixedCommand command = (APrefixedCommand) commands.get(commandName);

        if (command == null) {
            event.getMessage().delete().queue();
            event.getChannel().sendMessage("Commande non reconnue <@" + event.getAuthor().getId() + "> !").queue();
            return;
        }

        if (command.hasPermission(event.getMember())) {
            event.getChannel().sendMessage("Tu n'as pas la permission d'utiliser cette commande !").queue();
        } else {
            command.execute(event, args);
        }
    }

    public ACommand getCommand(String name) {
        return commands.get(name);
    }

    public Map<String, ACommand> getCommands() {
        return commands;
    }

    public static CommandManager getInstance() {
        if (commandManager == null) {
            commandManager = new CommandManager(Rene.getInstance().getJda());
        }
        return commandManager;
    }
}
