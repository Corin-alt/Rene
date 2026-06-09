package fr.corentin.rene.modules.games;

import fr.corentin.rene.commands.Permission;
import fr.corentin.rene.commands.parent.AInteractionCommand;
import fr.corentin.rene.utils.Channels;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;

import java.awt.*;

public class ClassementCommand extends AInteractionCommand {

    public ClassementCommand() {
        super("classement", "Affiche le classement des jeux", Permission.ALL, null);
    }

    @Override
    public boolean execute(GenericCommandInteractionEvent event) {
        if (!event.getChannel().getId().equals(Channels.GAMES.getChannelID())) {
            event.reply("Cette commande ne peut être utilisée que dans <#" + Channels.GAMES.getChannelID() + "> !")
                    .setEphemeral(true).queue();
            return true;
        }

        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("📊 Classement des jeux");
        embed.setDescription("Sélectionne un jeu dans le menu ci-dessous pour voir le classement.");
        embed.setColor(new Color(255, 200, 0));

        StringSelectMenu menu = StringSelectMenu.create("classement:select")
                .setPlaceholder("Choisis un jeu...")
                .addOption("Sudoku Facile", "sudoku:EASY", "🟢 Classement du Sudoku Facile")
                .addOption("Sudoku Moyen", "sudoku:MEDIUM", "🟡 Classement du Sudoku Moyen")
                .addOption("Sudoku Difficile", "sudoku:HARD", "🔴 Classement du Sudoku Difficile")
                .addOption("Mastermind", "mastermind", "🎯 Classement du Mastermind")
                .addOption("Sutom", "sutom", "🔤 Classement du Sutom")
                .build();

        event.replyEmbeds(embed.build())
                .setComponents(
                        ActionRow.of(menu),
                        ActionRow.of(
                                Button.primary("classement:daily:sudoku:EASY", "📅 Du jour"),
                                Button.secondary("classement:alltime:sudoku:EASY", "🏅 Total")
                        )
                )
                .queue();

        return true;
    }
}
