package fr.corentin.rene.modules.general.commands;

import fr.corentin.rene.commands.Permission;
import fr.corentin.rene.commands.parent.AUserInteractionCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;

import java.awt.*;

public class AvatarCommand extends AUserInteractionCommand {

    public AvatarCommand() {
        super("Voir Avatar", "Permet de récupérer l'avatar d'un utilisateur", Permission.ALL, null);
    }

    @Override
    public boolean execute(GenericCommandInteractionEvent event) {
        User user = ((UserContextInteractionEvent) event).getTarget();

        String avatarUrl = user.getEffectiveAvatarUrl();

        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("🕵️ Avatar de " + user.getEffectiveName());
        embed.setThumbnail(avatarUrl);
        embed.setDescription("Voici l'avatar demandé ainsi que l'url à laquelle le récupérer : " + avatarUrl);
        embed.setColor(Color.CYAN);

        event.replyEmbeds(embed.build()).setEphemeral(false).queue();

        return true;
    }
}