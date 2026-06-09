package fr.corentin.rene.modules.games.mastermind;

import fr.corentin.rene.events.parent.AEventListener;
import fr.corentin.rene.utils.Channels;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;
import net.dv8tion.jda.api.utils.messages.MessageEditData;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class MastermindInteractionListener extends AEventListener {
    private final MastermindGameManager gameManager;
    private final MastermindDatabaseService dbService;

    public MastermindInteractionListener(MastermindGameManager gameManager, MastermindDatabaseService dbService) {
        this.gameManager = gameManager;
        this.dbService = dbService;
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        String componentId = event.getComponentId();
        if (!componentId.startsWith("mastermind:")) return;

        String userId = event.getUser().getId();
        MastermindGame game = gameManager.getGame(userId);

        if (game == null) {
            event.reply("Tu n'as pas de partie en cours ! Utilise `/mastermind` pour commencer.")
                    .setEphemeral(true).queue();
            return;
        }

        switch (componentId) {
            case "mastermind:play" -> showGuessModal(event);
            case "mastermind:refresh" -> handleRefresh(event, game);
            case "mastermind:abandon" -> handleAbandon(event, game);
        }
    }

    @Override
    public void onModalInteraction(@NotNull ModalInteractionEvent event) {
        if (!event.getModalId().equals("mastermind:modal:guess")) return;

        String userId = event.getUser().getId();
        MastermindGame game = gameManager.getGame(userId);

        if (game == null) {
            event.reply("Tu n'as pas de partie en cours !").setEphemeral(true).queue();
            return;
        }

        ModalMapping mapping = event.getValue("mastermind:input:guess");
        if (mapping == null) return;

        String input = mapping.getAsString().trim().toUpperCase();
        String[] tokens = input.split("[\\s,]+");

        if (tokens.length != MastermindGame.CODE_LENGTH) {
            game.setLastFeedback("❌ Tu dois entrer exactement " + MastermindGame.CODE_LENGTH
                    + " couleurs ! (reçu : " + tokens.length + ")");
            event.deferEdit().queue();
            updateGameMessage(event, game);
            return;
        }

        MastermindColor[] guess = new MastermindColor[MastermindGame.CODE_LENGTH];
        for (int i = 0; i < MastermindGame.CODE_LENGTH; i++) {
            if (tokens[i].length() != 1) {
                game.setLastFeedback("❌ Chaque couleur doit être une seule lettre : " + tokens[i]);
                event.deferEdit().queue();
                updateGameMessage(event, game);
                return;
            }
            guess[i] = MastermindColor.fromCode(tokens[i].charAt(0));
            if (guess[i] == null) {
                game.setLastFeedback("❌ Couleur inconnue : **" + tokens[i] + "**\nCouleurs : R, B, V, J, P, O");
                event.deferEdit().queue();
                updateGameMessage(event, game);
                return;
            }
        }

        int[] feedback = game.submitGuess(guess);

        if (game.isCompleted()) {
            long timeSeconds = game.getElapsedSeconds();
            int attempts = game.getAttemptCount();

            dbService.insertScore(game.getUserId(), game.getDate(), attempts, timeSeconds);
            gameManager.removeGame(userId);

            event.deferEdit().queue();
            event.getUser().openPrivateChannel().flatMap(channel ->
                    channel.editMessageById(game.getDmMessageId(), buildCompletedMessage(game, timeSeconds))
            ).queue(success -> {}, failure -> {});

            postResultInChannel(event, game, timeSeconds, attempts);
        } else if (game.isFailed()) {
            gameManager.removeGame(userId);

            event.deferEdit().queue();
            event.getUser().openPrivateChannel().flatMap(channel ->
                    channel.editMessageById(game.getDmMessageId(), buildFailedMessage(game))
            ).queue(success -> {}, failure -> {});
        } else {
            game.setLastFeedback("🔴 " + feedback[0] + " bien placé(s)  ⚪ " + feedback[1] + " mal placé(s)");
            event.deferEdit().queue();
            updateGameMessage(event, game);
        }
    }

    private void showGuessModal(ButtonInteractionEvent event) {
        TextInput guessInput = TextInput.create("mastermind:input:guess", "Combinaison", TextInputStyle.SHORT)
                .setPlaceholder("R B V J (4 lettres séparées par des espaces)")
                .setRequired(true)
                .setMinLength(7)
                .setMaxLength(20)
                .build();

        Modal modal = Modal.create("mastermind:modal:guess", "Mastermind - Entrer votre combinaison")
                .addComponents(ActionRow.of(guessInput))
                .build();

        event.replyModal(modal).queue();
    }

    private void handleRefresh(ButtonInteractionEvent event, MastermindGame game) {
        game.setLastFeedback("⏱ Chrono mis à jour");
        event.editMessage(buildGameEditMessage(game)).queue();
    }

    private void handleAbandon(ButtonInteractionEvent event, MastermindGame game) {
        gameManager.removeGame(game.getUserId());
        event.editMessage(buildAbandonMessage(game)).queue();
    }

    private void updateGameMessage(ModalInteractionEvent event, MastermindGame game) {
        event.getUser().openPrivateChannel().flatMap(channel ->
                channel.editMessageById(game.getDmMessageId(), buildGameEditMessage(game))
        ).queue(success -> {}, failure -> {});
    }

    private MessageEditData buildGameEditMessage(MastermindGame game) {
        String dateStr = game.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("🎯 Mastermind du jour - " + dateStr);

        StringBuilder desc = new StringBuilder();
        desc.append("**🎨 Couleurs :** ").append(MastermindColor.allCodesDisplay()).append("\n\n");
        desc.append(MastermindRenderer.render(game));
        embed.setDescription(desc.toString());

        embed.setColor(new Color(255, 100, 0));
        embed.addField("⏱ Chrono", game.getFormattedTime(), true);
        embed.addField("🎯 Essais", game.getAttemptCount() + "/" + MastermindGame.MAX_ATTEMPTS, true);

        String feedback = game.getLastFeedback();
        if (feedback != null && !feedback.isEmpty()) {
            embed.addField("💬 Info", feedback, false);
        }

        embed.setFooter("Format : R B V J (initiales des couleurs)");

        return new MessageEditBuilder()
                .setEmbeds(embed.build())
                .setComponents(ActionRow.of(
                        Button.primary("mastermind:play", "Jouer"),
                        Button.secondary("mastermind:refresh", "🔄"),
                        Button.danger("mastermind:abandon", "Abandonner")
                ))
                .build();
    }

    private MessageEditData buildCompletedMessage(MastermindGame game, long timeSeconds) {
        String dateStr = game.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("🏆 Mastermind complété ! - " + dateStr);
        embed.setDescription(MastermindRenderer.render(game));
        embed.setColor(new Color(0, 200, 0));
        embed.addField("⏱ Temps final", MastermindGame.formatTime(timeSeconds), true);
        embed.addField("🎯 Essais", game.getAttemptCount() + "/" + MastermindGame.MAX_ATTEMPTS, true);
        embed.addField("💬 Info", "Bravo ! Ton résultat a été envoyé dans le serveur.", false);

        return new MessageEditBuilder()
                .setEmbeds(embed.build())
                .setComponents()
                .build();
    }

    private MessageEditData buildFailedMessage(MastermindGame game) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("😔 Mastermind échoué");
        embed.setDescription("Tu n'as pas trouvé la combinaison en " + MastermindGame.MAX_ATTEMPTS + " essais.\n\n"
                + MastermindRenderer.render(game)
                + "\nLa combinaison était : " + MastermindRenderer.renderSecret(game.getSecret()));
        embed.setColor(new Color(200, 0, 0));

        return new MessageEditBuilder()
                .setEmbeds(embed.build())
                .setComponents()
                .build();
    }

    private MessageEditData buildAbandonMessage(MastermindGame game) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("Mastermind abandonné");
        embed.setDescription("La combinaison était : " + MastermindRenderer.renderSecret(game.getSecret()));
        embed.setColor(new Color(200, 0, 0));

        return new MessageEditBuilder()
                .setEmbeds(embed.build())
                .setComponents()
                .build();
    }

    private void postResultInChannel(ModalInteractionEvent event, MastermindGame game, long timeSeconds, int attempts) {
        TextChannel channel = event.getJDA().getTextChannelById(game.getGuildChannelId());
        if (channel == null) return;

        List<MastermindDatabaseService.ScoreEntry> scores = dbService.getDailyScores(game.getDate());
        String dateStr = game.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("🏆 Mastermind - " + dateStr);
        embed.setColor(new Color(255, 200, 0));
        embed.setDescription("🎯 <@" + game.getUserId() + "> a trouvé la combinaison en **"
                + attempts + "/" + MastermindGame.MAX_ATTEMPTS + " essais** ("
                + MastermindGame.formatTime(timeSeconds) + ") !");

        StringBuilder lb = new StringBuilder();
        String[] medals = {"🥇", "🥈", "🥉"};
        for (int i = 0; i < scores.size(); i++) {
            MastermindDatabaseService.ScoreEntry score = scores.get(i);
            String prefix = i < 3 ? medals[i] : (i + 1) + ".";
            lb.append(prefix).append(" <@").append(score.userId()).append("> - **")
                    .append(score.attempts()).append("/").append(MastermindGame.MAX_ATTEMPTS)
                    .append("** en ").append(MastermindGame.formatTime(score.timeSeconds())).append("\n");
        }

        if (!lb.isEmpty()) {
            embed.addField("📊 Classement du jour", lb.toString(), false);
        }

        channel.sendMessageEmbeds(embed.build()).queue();
    }
}
