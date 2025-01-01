package fr.corentin.rene.modules.general.commands;

import fr.corentin.rene.commands.Permission;
import fr.corentin.rene.commands.parent.AInteractionCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;

import java.awt.*;

public class PingCommand extends AInteractionCommand {

    public PingCommand() {
        super("ping", "Vérifie le temps de réponse de Rene", Permission.ALL, null);
    }

    @Override
    public boolean execute(GenericCommandInteractionEvent event) {
        long time = System.currentTimeMillis();
        event.reply("Pinging...").setEphemeral(true).queue(response -> {
            long ping = System.currentTimeMillis() - time;
            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setTitle("Pong!");
            embedBuilder.setDescription("Response Time: " + ping + " ms");
            embedBuilder.setColor(Color.GREEN);
            embedBuilder.setFooter("Rene");
            response.editOriginalEmbeds(embedBuilder.build()).queue();
        });
        return true;
    }
}
