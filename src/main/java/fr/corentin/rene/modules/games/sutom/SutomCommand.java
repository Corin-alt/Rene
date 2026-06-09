package fr.corentin.rene.modules.games.sutom;

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

public class SutomCommand extends AInteractionCommand {
    private final SutomGameManager gameManager;
    private final SutomDatabaseService dbService;

    public SutomCommand(SutomGameManager gameManager, SutomDatabaseService dbService) {
        super("sutom", "Joue au Sutom du jour !", Permission.ALL, null);
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
            event.reply("Tu as déjà complété le Sutom du jour !").setEphemeral(true).queue();
            return true;
        }

        event.deferReply(true).queue();

        SutomGame game = gameManager.startGame(userId, Channels.GAMES.getChannelID());

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

    static MessageCreateData buildGameMessage(SutomGame game) {
        String dateStr = game.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("🔤 Sutom du jour - " + dateStr);
        embed.setDescription("""
                **📖 Règles du Sutom**
                Trouve le mot du jour en **6 essais** maximum !

                **Indices après chaque essai :**
                🟥 = lettre **correcte**, bonne position
                🟡 = lettre **présente**, mauvaise position
                🟦 = lettre **absente** du mot

                **🎮 Comment jouer ?**
                Clique sur **Jouer** et propose un mot de **"""
                + game.getWordLength() + " lettres** commençant par **" + game.getFirstLetter() + """
                **.

                """ + SutomRenderer.render(game));
        embed.setColor(new Color(220, 50, 50));
        embed.addField("⏱ Chrono", game.getFormattedTime(), true);
        embed.addField("💭 Essais", game.getAttemptCount() + "/" + SutomGame.MAX_ATTEMPTS, true);
        embed.addField("🔠 Longueur", game.getWordLength() + " lettres", true);

        String feedback = game.getLastFeedback();
        if (feedback != null && !feedback.isEmpty()) {
            embed.addField("💬 Info", feedback, false);
        }

        embed.setFooter("Le mot commence par " + game.getFirstLetter() + " et fait " + game.getWordLength() + " lettres");

        return new MessageCreateBuilder()
                .setEmbeds(embed.build())
                .setComponents(ActionRow.of(
                        Button.primary("sutom:play", "Jouer"),
                        Button.secondary("sutom:refresh", "🔄"),
                        Button.danger("sutom:abandon", "Abandonner")
                ))
                .build();
    }
}
