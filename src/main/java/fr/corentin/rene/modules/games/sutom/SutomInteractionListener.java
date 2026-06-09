package fr.corentin.rene.modules.games.sutom;

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

public class SutomInteractionListener extends AEventListener {
    private final SutomGameManager gameManager;
    private final SutomDatabaseService dbService;

    public SutomInteractionListener(SutomGameManager gameManager, SutomDatabaseService dbService) {
        this.gameManager = gameManager;
        this.dbService = dbService;
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        String componentId = event.getComponentId();
        if (!componentId.startsWith("sutom:")) return;

        String userId = event.getUser().getId();
        SutomGame game = gameManager.getGame(userId);

        if (game == null) {
            event.reply("Tu n'as pas de partie en cours ! Utilise `/sutom` pour commencer.")
                    .setEphemeral(true).queue();
            return;
        }

        switch (componentId) {
            case "sutom:play" -> showGuessModal(event, game);
            case "sutom:refresh" -> handleRefresh(event, game);
            case "sutom:abandon" -> handleAbandon(event, game);
        }
    }

    @Override
    public void onModalInteraction(@NotNull ModalInteractionEvent event) {
        if (!event.getModalId().equals("sutom:modal:guess")) return;

        String userId = event.getUser().getId();
        SutomGame game = gameManager.getGame(userId);

        if (game == null) {
            event.reply("Tu n'as pas de partie en cours !").setEphemeral(true).queue();
            return;
        }

        ModalMapping mapping = event.getValue("sutom:input:guess");
        if (mapping == null) return;

        String input = mapping.getAsString().trim().toUpperCase().replaceAll("[^A-Z]", "");

        if (input.length() != game.getWordLength()) {
            game.setLastFeedback("❌ Le mot doit faire **" + game.getWordLength()
                    + " lettres** ! (reçu : " + input.length() + ")");
            event.deferEdit().queue();
            updateGameMessage(event, game);
            return;
        }

        if (input.charAt(0) != game.getFirstLetter()) {
            game.setLastFeedback("❌ Le mot doit commencer par **" + game.getFirstLetter() + "** !");
            event.deferEdit().queue();
            updateGameMessage(event, game);
            return;
        }

        SutomLetterResult[] result = game.submitGuess(input);

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
            int correct = 0;
            int present = 0;
            for (SutomLetterResult r : result) {
                if (r == SutomLetterResult.CORRECT) correct++;
                else if (r == SutomLetterResult.PRESENT) present++;
            }
            game.setLastFeedback("🟥 " + correct + " correcte(s)  🟡 " + present + " mal placée(s)");
            event.deferEdit().queue();
            updateGameMessage(event, game);
        }
    }

    private void showGuessModal(ButtonInteractionEvent event, SutomGame game) {
        TextInput guessInput = TextInput.create("sutom:input:guess", "Mot", TextInputStyle.SHORT)
                .setPlaceholder("Mot de " + game.getWordLength() + " lettres commençant par " + game.getFirstLetter())
                .setRequired(true)
                .setMinLength(game.getWordLength())
                .setMaxLength(game.getWordLength())
                .build();

        Modal modal = Modal.create("sutom:modal:guess", "Sutom - Proposer un mot")
                .addComponents(ActionRow.of(guessInput))
                .build();

        event.replyModal(modal).queue();
    }

    private void handleRefresh(ButtonInteractionEvent event, SutomGame game) {
        game.setLastFeedback("⏱ Chrono mis à jour");
        event.editMessage(buildGameEditMessage(game)).queue();
    }

    private void handleAbandon(ButtonInteractionEvent event, SutomGame game) {
        gameManager.removeGame(game.getUserId());
        event.editMessage(buildAbandonMessage(game)).queue();
    }

    private void updateGameMessage(ModalInteractionEvent event, SutomGame game) {
        event.getUser().openPrivateChannel().flatMap(channel ->
                channel.editMessageById(game.getDmMessageId(), buildGameEditMessage(game))
        ).queue(success -> {}, failure -> {});
    }

    private MessageEditData buildGameEditMessage(SutomGame game) {
        String dateStr = game.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("🔤 Sutom du jour - " + dateStr);

        StringBuilder desc = new StringBuilder();
        desc.append("Le mot fait **").append(game.getWordLength())
                .append(" lettres** et commence par **").append(game.getFirstLetter()).append("**\n\n");
        desc.append(SutomRenderer.render(game));
        embed.setDescription(desc.toString());

        embed.setColor(new Color(220, 50, 50));
        embed.addField("⏱ Chrono", game.getFormattedTime(), true);
        embed.addField("💭 Essais", game.getAttemptCount() + "/" + SutomGame.MAX_ATTEMPTS, true);

        String feedback = game.getLastFeedback();
        if (feedback != null && !feedback.isEmpty()) {
            embed.addField("💬 Info", feedback, false);
        }

        embed.setFooter("Le mot commence par " + game.getFirstLetter() + " et fait " + game.getWordLength() + " lettres");

        return new MessageEditBuilder()
                .setEmbeds(embed.build())
                .setComponents(ActionRow.of(
                        Button.primary("sutom:play", "Jouer"),
                        Button.secondary("sutom:refresh", "🔄"),
                        Button.danger("sutom:abandon", "Abandonner")
                ))
                .build();
    }

    private MessageEditData buildCompletedMessage(SutomGame game, long timeSeconds) {
        String dateStr = game.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("🏆 Sutom complété ! - " + dateStr);
        embed.setDescription(SutomRenderer.render(game));
        embed.setColor(new Color(0, 200, 0));
        embed.addField("⏱ Temps final", SutomGame.formatTime(timeSeconds), true);
        embed.addField("💭 Essais", game.getAttemptCount() + "/" + SutomGame.MAX_ATTEMPTS, true);
        embed.addField("💬 Info", "Bravo ! Ton résultat a été envoyé dans le serveur.", false);

        return new MessageEditBuilder()
                .setEmbeds(embed.build())
                .setComponents()
                .build();
    }

    private MessageEditData buildFailedMessage(SutomGame game) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("😔 Sutom échoué");
        embed.setDescription("Tu n'as pas trouvé le mot en " + SutomGame.MAX_ATTEMPTS + " essais.\n\n"
                + SutomRenderer.render(game)
                + "\nLe mot était : **" + game.getTargetWord() + "**");
        embed.setColor(new Color(200, 0, 0));

        return new MessageEditBuilder()
                .setEmbeds(embed.build())
                .setComponents()
                .build();
    }

    private MessageEditData buildAbandonMessage(SutomGame game) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("Sutom abandonné");
        embed.setDescription("Le mot était : **" + game.getTargetWord() + "**");
        embed.setColor(new Color(200, 0, 0));

        return new MessageEditBuilder()
                .setEmbeds(embed.build())
                .setComponents()
                .build();
    }

    private void postResultInChannel(ModalInteractionEvent event, SutomGame game, long timeSeconds, int attempts) {
        TextChannel channel = event.getJDA().getTextChannelById(game.getGuildChannelId());
        if (channel == null) return;

        List<SutomDatabaseService.ScoreEntry> scores = dbService.getDailyScores(game.getDate());
        String dateStr = game.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("🏆 Sutom - " + dateStr);
        embed.setColor(new Color(255, 200, 0));
        embed.setDescription("🔤 <@" + game.getUserId() + "> a trouvé le mot en **"
                + attempts + "/" + SutomGame.MAX_ATTEMPTS + " essais** ("
                + SutomGame.formatTime(timeSeconds) + ") !");

        StringBuilder lb = new StringBuilder();
        String[] medals = {"🥇", "🥈", "🥉"};
        for (int i = 0; i < scores.size(); i++) {
            SutomDatabaseService.ScoreEntry score = scores.get(i);
            String prefix = i < 3 ? medals[i] : (i + 1) + ".";
            lb.append(prefix).append(" <@").append(score.userId()).append("> - **")
                    .append(score.attempts()).append("/").append(SutomGame.MAX_ATTEMPTS)
                    .append("** en ").append(SutomGame.formatTime(score.timeSeconds())).append("\n");
        }

        if (!lb.isEmpty()) {
            embed.addField("📊 Classement du jour", lb.toString(), false);
        }

        channel.sendMessageEmbeds(embed.build()).queue();
    }
}
