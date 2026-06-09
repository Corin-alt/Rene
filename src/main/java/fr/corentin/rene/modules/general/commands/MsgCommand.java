package fr.corentin.rene.modules.general.commands;

import fr.corentin.rene.commands.Permission;
import fr.corentin.rene.commands.parent.AInteractionCommand;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.List;

public class MsgCommand extends AInteractionCommand {

    public MsgCommand() {
        super("msg", "Envoyer un message en tant que le bot", Permission.ADMIN, List.of(
                new OptionData(OptionType.STRING, "message", "Le message à envoyer", true)
        ));
    }

    @Override
    public boolean execute(GenericCommandInteractionEvent event) {
        String message = event.getOption("message").getAsString();
        event.getMessageChannel().sendMessage(message).queue();
        event.reply("Message envoyé !").setEphemeral(true).queue(hook -> hook.deleteOriginal().queue());
        return true;
    }
}
