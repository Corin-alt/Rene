package fr.corentin.rene.modules.games.sudoku;

import fr.corentin.rene.commands.Permission;
import fr.corentin.rene.commands.parent.AInteractionCommand;
import fr.corentin.rene.utils.Channels;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import java.awt.*;

public class SudokuCommand extends AInteractionCommand {
    private final SudokuGameManager gameManager;

    public SudokuCommand(SudokuGameManager gameManager) {
        super("sudoku", "Joue au Sudoku du jour !", Permission.ALL, null);
        this.gameManager = gameManager;
    }

    @Override
    public boolean execute(GenericCommandInteractionEvent event) {
        String userId = event.getUser().getId();

        if (!event.getChannel().getId().equals(Channels.GAMES.getChannelID())) {
            event.reply("Cette commande ne peut être utilisée que dans <#" + Channels.GAMES.getChannelID() + "> !")
                    .setEphemeral(true).queue();
            return true;
        }

        if (gameManager.hasActiveGame(userId)) {
            event.reply("Tu as déjà une partie en cours ! Regarde tes MPs.").setEphemeral(true).queue();
            return true;
        }

        event.deferReply(true).queue();

        event.getUser().openPrivateChannel().queue(
                channel -> {
                    MessageCreateData message = buildDifficultyMessage();
                    channel.sendMessage(message).queue(
                            msg -> event.getHook().editOriginal("C'est parti ! Regarde tes MPs pour choisir ta difficulté !").queue(),
                            error -> event.getHook().editOriginal("Je n'ai pas pu t'envoyer de MP ! Vérifie tes paramètres de confidentialité.").queue()
                    );
                },
                error -> event.getHook().editOriginal("Je n'ai pas pu t'envoyer de MP !").queue()
        );

        return true;
    }

    private MessageCreateData buildDifficultyMessage() {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("Sudoku du jour");
        embed.setDescription("""
                **📖 Règles du Sudoku**
                Remplis la grille 9x9 pour que chaque **ligne**, chaque **colonne** et chaque **bloc 3x3** contienne les chiffres de **1 à 9** sans répétition.

                **🎮 Comment jouer ?**
                Clique sur **✏️ Jouer** puis entre tes coups au format `A1=5 B3=7`.
                • La lettre (A-I) = la ligne, le chiffre (1-9) = la colonne
                • Mets `=0` pour effacer une case (ex: `A1=0`)
                • Tu peux entrer plusieurs coups d'un coup !

                **⚡ Choisis ta difficulté :**
                🟢 **Facile** — 35 cases à remplir
                🟡 **Moyen** — 45 cases à remplir
                🔴 **Difficile** — 55 cases à remplir

                Tu peux jouer les 3 difficultés chaque jour.
                """);
        embed.setColor(new Color(0, 150, 255));
        embed.addField("⏱ Chrono", "Le chronomètre démarre dès que tu choisis une difficulté et s'arrête quand tu valides.", false);
        embed.addField("🏆 Classement", "À la fin de la partie, ton temps et ton pseudo sont affichés dans le classement du jour sur le serveur.", false);
        embed.addField("🔒 RGPD", "En jouant, tu acceptes que ton identifiant Discord et ton temps soient enregistrés et affichés dans le classement du serveur. Ces données sont stockées localement par le bot. Tu peux demander leur suppression à Corin.", false);
        embed.setFooter("Un nouveau Sudoku chaque jour !");

        return new MessageCreateBuilder()
                .setEmbeds(embed.build())
                .setComponents(ActionRow.of(
                        Button.success("sudoku:diff:EASY", "🟢 Facile"),
                        Button.primary("sudoku:diff:MEDIUM", "🟡 Moyen"),
                        Button.danger("sudoku:diff:HARD", "🔴 Difficile")
                ))
                .build();
    }
}
