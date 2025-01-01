package fr.corentin.rene.modules.general.commands;

import fr.corentin.rene.Rene;
import fr.corentin.rene.commands.Permission;
import fr.corentin.rene.commands.parent.AInteractionCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;

import java.awt.*;
import java.time.Duration;
import java.time.Instant;

public class UptimeCommand extends AInteractionCommand {

    public UptimeCommand() {
        super("uptime", "Affiche la durée depuis laquelle Rene est démarré", Permission.ALL, null);
    }

    @Override
    public boolean execute(GenericCommandInteractionEvent event) {
        Duration duration = Duration.between(Rene.getInstance().getStartTime(), Instant.now());
        long hours = duration.toHours();
        long minutes = duration.toMinutes() % 60;
        long seconds = duration.getSeconds() % 60;

        String uptime = String.format("%d heures, %d minutes et %d secondes", hours, minutes, seconds);

        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("📡 Uptime");
        embed.setDescription(uptime);
        embed.setColor(Color.CYAN);

        event.replyEmbeds(embed.build()).setEphemeral(false).queue();
        return true;
    }
}