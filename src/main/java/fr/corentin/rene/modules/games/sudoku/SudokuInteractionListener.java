package fr.corentin.rene.modules.games.sudoku;

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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class SudokuInteractionListener extends AEventListener {
    private final SudokuGameManager gameManager;
    private final SudokuDatabaseService dbService;

    public SudokuInteractionListener(SudokuGameManager gameManager, SudokuDatabaseService dbService) {
        this.gameManager = gameManager;
        this.dbService = dbService;
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        String componentId = event.getComponentId();
        if (!componentId.startsWith("sudoku:")) return;

        String userId = event.getUser().getId();

        if (componentId.startsWith("sudoku:diff:")) {
            handleDifficultySelection(event, userId, componentId);
            return;
        }

        SudokuGame game = gameManager.getGame(userId);
        if (game == null) {
            event.reply("Tu n'as pas de partie en cours ! Utilise `/sudoku` pour commencer.")
                    .setEphemeral(true).queue();
            return;
        }

        switch (componentId) {
            case "sudoku:play" -> showMoveModal(event);
            case "sudoku:refresh" -> handleRefresh(event, game);
            case "sudoku:validate" -> handleValidate(event, game);
            case "sudoku:abandon" -> handleAbandon(event, game);
        }
    }

    @Override
    public void onModalInteraction(@NotNull ModalInteractionEvent event) {
        if (!event.getModalId().equals("sudoku:modal:move")) return;

        String userId = event.getUser().getId();
        SudokuGame game = gameManager.getGame(userId);

        if (game == null) {
            event.reply("Tu n'as pas de partie en cours !").setEphemeral(true).queue();
            return;
        }

        ModalMapping mapping = event.getValue("sudoku:input:moves");
        if (mapping == null) return;

        String input = mapping.getAsString().trim().toUpperCase();
        String[] tokens = input.split("[\\s,]+");

        int applied = 0;
        int errors = 0;
        StringBuilder feedback = new StringBuilder();

        for (String token : tokens) {
            if (token.isEmpty()) continue;

            if (!token.matches("[A-I][1-9]=[0-9]")) {
                errors++;
                feedback.append("❌ Format invalide : ").append(token).append("\n");
                continue;
            }

            int row = token.charAt(0) - 'A';
            int col = token.charAt(1) - '1';
            int value = token.charAt(3) - '0';

            if (value == 0) {
                if (game.eraseCell(row, col)) {
                    applied++;
                } else {
                    errors++;
                    feedback.append("❌ ").append(token, 0, 2).append(" : case fixe ou vide\n");
                }
            } else {
                if (game.isGivenCell(row, col)) {
                    errors++;
                    feedback.append("❌ ").append(token, 0, 2).append(" : case fixe\n");
                } else {
                    game.placeNumber(row, col, value);
                    applied++;
                }
            }
        }

        if (applied > 0 && errors == 0) {
            game.setLastFeedback("✅ " + applied + " coup(s) appliqué(s)");
        } else if (applied > 0) {
            game.setLastFeedback("✅ " + applied + " coup(s) — ❌ " + errors + " erreur(s)\n" + feedback);
        } else if (errors > 0) {
            game.setLastFeedback("❌ " + errors + " erreur(s)\n" + feedback);
        } else {
            game.setLastFeedback("Aucun coup détecté. Format : A1=5 B3=7");
        }

        event.deferEdit().queue();
        updateGridMessage(event, game);
    }

    private void handleDifficultySelection(ButtonInteractionEvent event, String userId, String componentId) {
        String[] parts = componentId.split(":");
        if (parts.length < 3) return;

        String difficultyName = parts[2];

        SudokuDifficulty difficulty;
        try {
            difficulty = SudokuDifficulty.valueOf(difficultyName);
        } catch (IllegalArgumentException e) {
            return;
        }

        if (gameManager.hasActiveGame(userId)) {
            event.reply("Tu as déjà une partie en cours !").setEphemeral(true).queue();
            return;
        }

        LocalDate today = LocalDate.now();
        if (dbService.hasCompletedToday(userId, today, difficulty)) {
            event.reply("Tu as déjà complété le Sudoku " + difficulty.getLabel() + " du jour !")
                    .setEphemeral(true).queue();
            return;
        }

        SudokuGame game = gameManager.startGame(userId, Channels.GAMES.getChannelID(), difficulty);
        game.setDmMessageId(event.getMessageIdLong());

        event.editMessage(buildGameEditMessage(game)).queue();
    }

    private void showMoveModal(ButtonInteractionEvent event) {
        TextInput movesInput = TextInput.create("sudoku:input:moves", "Coups", TextInputStyle.PARAGRAPH)
                .setPlaceholder("A1=5 B3=7 C9=1 (0 pour effacer)")
                .setRequired(true)
                .setMinLength(4)
                .setMaxLength(500)
                .build();

        Modal modal = Modal.create("sudoku:modal:move", "Sudoku - Entrer vos coups")
                .addComponents(ActionRow.of(movesInput))
                .build();

        event.replyModal(modal).queue();
    }

    private void handleRefresh(ButtonInteractionEvent event, SudokuGame game) {
        game.setLastFeedback("⏱ Chrono mis à jour");
        event.editMessage(buildGameEditMessage(game)).queue();
    }

    private void handleValidate(ButtonInteractionEvent event, SudokuGame game) {
        int remaining = game.countRemaining();
        if (remaining > 0) {
            game.setLastFeedback("⚠️ Grille incomplète ! Il reste " + remaining + " case(s) vide(s).");
            event.editMessage(buildGameEditMessage(game)).queue();
            return;
        }

        int errors = game.countErrors();
        if (errors > 0) {
            game.setLastFeedback("❌ Il y a " + errors + " erreur(s) dans ta grille !");
            event.editMessage(buildGameEditMessage(game)).queue();
            return;
        }

        long timeSeconds = game.getElapsedSeconds();
        int moveCount = game.getMoveCount();

        dbService.insertScore(game.getUserId(), game.getDate(), game.getDifficulty(), timeSeconds, moveCount);
        gameManager.removeGame(game.getUserId());

        event.editMessage(buildCompletedMessage(game, timeSeconds)).queue();
        postResultInChannel(event, game, timeSeconds, moveCount);
    }

    private void handleAbandon(ButtonInteractionEvent event, SudokuGame game) {
        gameManager.removeGame(game.getUserId());
        event.editMessage(buildAbandonMessage(game)).queue();
    }

    private void updateGridMessage(ModalInteractionEvent event, SudokuGame game) {
        event.getUser().openPrivateChannel().flatMap(channel ->
                channel.editMessageById(game.getDmMessageId(), buildGameEditMessage(game))
        ).queue(success -> {}, failure -> {});
    }

    private MessageEditData buildGameEditMessage(SudokuGame game) {
        String dateStr = game.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        SudokuDifficulty diff = game.getDifficulty();

        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle(diff.getEmoji() + " Sudoku " + diff.getLabel() + " - " + dateStr);
        embed.setDescription(SudokuRenderer.render(game));
        embed.setColor(new Color(0, 150, 255));
        embed.addField("⏱ Chrono", game.getFormattedTime(), true);
        embed.addField("✏️ Coups", String.valueOf(game.getMoveCount()), true);
        embed.addField("📝 Restant", String.valueOf(game.countRemaining()), true);

        String feedback = game.getLastFeedback();
        if (feedback != null && !feedback.isEmpty()) {
            embed.addField("💬 Info", feedback, false);
        }

        embed.setFooter("Format: A1=5 B3=7 (lettre + colonne = valeur, 0 pour effacer)");

        return new MessageEditBuilder()
                .setEmbeds(embed.build())
                .setComponents(ActionRow.of(
                        Button.primary("sudoku:play", "Jouer"),
                        Button.secondary("sudoku:refresh", "🔄"),
                        Button.success("sudoku:validate", "Valider"),
                        Button.danger("sudoku:abandon", "Abandonner")
                ))
                .build();
    }

    private MessageEditData buildCompletedMessage(SudokuGame game, long timeSeconds) {
        String dateStr = game.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        SudokuDifficulty diff = game.getDifficulty();

        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("🏆 Sudoku " + diff.getLabel() + " complété ! - " + dateStr);
        embed.setDescription(SudokuRenderer.render(game));
        embed.setColor(new Color(0, 200, 0));
        embed.addField("⏱ Temps final", SudokuGame.formatTime(timeSeconds), true);
        embed.addField("✏️ Coups", String.valueOf(game.getMoveCount()), true);
        embed.addField("💬 Info", "Bravo ! Ton résultat a été envoyé dans le serveur.", false);

        return new MessageEditBuilder()
                .setEmbeds(embed.build())
                .setComponents()
                .build();
    }

    private MessageEditData buildAbandonMessage(SudokuGame game) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("Sudoku abandonné");
        embed.setDescription("Voici la solution :\n" + SudokuRenderer.renderSolution(game.getSolution()));
        embed.setColor(new Color(200, 0, 0));

        return new MessageEditBuilder()
                .setEmbeds(embed.build())
                .setComponents()
                .build();
    }

    private void postResultInChannel(ButtonInteractionEvent event, SudokuGame game, long timeSeconds, int moveCount) {
        TextChannel channel = event.getJDA().getTextChannelById(game.getGuildChannelId());
        if (channel == null) return;

        SudokuDifficulty diff = game.getDifficulty();
        List<SudokuDatabaseService.ScoreEntry> scores = dbService.getDailyScores(game.getDate(), diff);
        String dateStr = game.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("🏆 Sudoku " + diff.getLabel() + " - " + dateStr);
        embed.setColor(new Color(255, 200, 0));
        embed.setDescription(diff.getEmoji() + " <@" + game.getUserId() + "> a résolu le Sudoku " + diff.getLabel()
                + " en **" + SudokuGame.formatTime(timeSeconds) + "** avec **" + moveCount + " coups** !");

        StringBuilder lb = new StringBuilder();
        String[] medals = {"🥇", "🥈", "🥉"};
        for (int i = 0; i < scores.size(); i++) {
            SudokuDatabaseService.ScoreEntry score = scores.get(i);
            String prefix = i < 3 ? medals[i] : (i + 1) + ".";
            lb.append(prefix).append(" <@").append(score.userId()).append("> - **")
                    .append(SudokuGame.formatTime(score.timeSeconds()))
                    .append("** (").append(score.moveCount()).append(" coups)\n");
        }

        if (!lb.isEmpty()) {
            embed.addField("📊 Classement du jour", lb.toString(), false);
        }

        channel.sendMessageEmbeds(embed.build()).queue();
    }
}
