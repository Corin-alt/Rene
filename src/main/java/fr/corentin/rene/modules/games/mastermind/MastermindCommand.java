package fr.corentin.rene.modules.games.mastermind;

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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class MastermindCommand extends AInteractionCommand {
    private final MastermindGameManager gameManager;
    private final MastermindDatabaseService dbService;

    public MastermindCommand(MastermindGameManager gameManager, MastermindDatabaseService dbService) {
        super("mastermind", "Joue au Mastermind du jour !", Permission.ALL, null);
        this.gameManager = gameManager;
        this.dbService = dbService;
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

        if (dbService.hasCompletedToday(userId, LocalDate.now())) {
            event.reply("Tu as déjà complété le Mastermind du jour !").setEphemeral(true).queue();
            return true;
        }

        event.deferReply(true).queue();

        MastermindGame game = gameManager.startGame(userId, Channels.GAMES.getChannelID());

        event.getUser().openPrivateChannel().queue(
                channel -> {
                    MessageCreateData message = buildGameMessage(game);
                    channel.sendMessage(message).queue(
                            msg -> {
                                game.setDmMessageId(msg.getIdLong());
                                event.getHook().editOriginal("C'est parti ! Regarde tes MPs !").queue();
                            },
                            error -> {
                                gameManager.removeGame(userId);
                                event.getHook().editOriginal("Je n'ai pas pu t'envoyer de MP ! Vérifie tes paramètres de confidentialité.").queue();
                            }
                    );
                },
                error -> {
                    gameManager.removeGame(userId);
                    event.getHook().editOriginal("Je n'ai pas pu t'envoyer de MP !").queue();
                }
        );

        return true;
    }

    static MessageCreateData buildGameMessage(MastermindGame game) {
        String dateStr = game.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("🎯 Mastermind du jour - " + dateStr);
        embed.setDescription("""
                **🎲 Règles du Mastermind**
                Trouve la combinaison secrète de **4 couleurs** parmi 6.
                Les couleurs ne se répètent pas.

                **Indices après chaque essai :**
                🔴 = bonne couleur, **bonne position**
                ⚪ = bonne couleur, **mauvaise position**
                ⚫ = couleur absente

                **🎨 Couleurs disponibles :**
                """ + MastermindColor.allCodesDisplay() + """


                **🎮 Comment jouer ?**
                Clique sur **Jouer** et entre 4 lettres : `R B V J`

                """ + MastermindRenderer.render(game));
        embed.setColor(new Color(255, 100, 0));
        embed.addField("⏱ Chrono", game.getFormattedTime(), true);
        embed.addField("🎯 Essais", game.getAttemptCount() + "/" + MastermindGame.MAX_ATTEMPTS, true);

        String feedback = game.getLastFeedback();
        if (feedback != null && !feedback.isEmpty()) {
            embed.addField("💬 Info", feedback, false);
        }

        embed.setFooter("Format : R B V J (initiales des couleurs, séparées par des espaces)");

        return new MessageCreateBuilder()
                .setEmbeds(embed.build())
                .setComponents(ActionRow.of(
                        Button.primary("mastermind:play", "Jouer"),
                        Button.secondary("mastermind:refresh", "🔄"),
                        Button.danger("mastermind:abandon", "Abandonner")
                ))
                .build();
    }
}
