package fr.corentin.rene.modules.games;

import fr.corentin.rene.events.parent.AEventListener;
import fr.corentin.rene.modules.games.mastermind.MastermindDatabaseService;
import fr.corentin.rene.modules.games.mastermind.MastermindGame;
import fr.corentin.rene.modules.games.sudoku.SudokuDatabaseService;
import fr.corentin.rene.modules.games.sudoku.SudokuDifficulty;
import fr.corentin.rene.modules.games.sudoku.SudokuGame;
import fr.corentin.rene.modules.games.sutom.SutomDatabaseService;
import fr.corentin.rene.modules.games.sutom.SutomGame;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;
import net.dv8tion.jda.api.utils.messages.MessageEditData;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ClassementInteractionListener extends AEventListener {
    private final SudokuDatabaseService sudokuDbService;
    private final MastermindDatabaseService mastermindDbService;
    private final SutomDatabaseService sutomDbService;

    public ClassementInteractionListener(SudokuDatabaseService sudokuDbService,
                                         MastermindDatabaseService mastermindDbService,
                                         SutomDatabaseService sutomDbService) {
        this.sudokuDbService = sudokuDbService;
        this.mastermindDbService = mastermindDbService;
        this.sutomDbService = sutomDbService;
    }

    @Override
    public void onStringSelectInteraction(@NotNull StringSelectInteractionEvent event) {
        if (!event.getComponentId().equals("classement:select")) return;

        String selected = event.getValues().get(0);
        MessageEditData data = buildView(selected, true);
        if (data != null) {
            event.editMessage(data).queue();
        }
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        String id = event.getComponentId();
        if (!id.startsWith("classement:daily:") && !id.startsWith("classement:alltime:")) return;

        boolean daily = id.startsWith("classement:daily:");
        String gameKey = id.substring(daily ? "classement:daily:".length() : "classement:alltime:".length());

        MessageEditData data = buildView(gameKey, daily);
        if (data != null) {
            event.editMessage(data).queue();
        }
    }

    private MessageEditData buildView(String gameKey, boolean daily) {
        String[] parts = gameKey.split(":");
        String game = parts[0];

        EmbedBuilder embed;
        if ("sudoku".equals(game)) {
            if (parts.length < 2) return null;
            SudokuDifficulty difficulty;
            try {
                difficulty = SudokuDifficulty.valueOf(parts[1]);
            } catch (IllegalArgumentException e) {
                return null;
            }
            embed = daily ? buildDailySudokuEmbed(difficulty) : buildAllTimeSudokuEmbed(difficulty);
        } else if ("mastermind".equals(game)) {
            embed = daily ? buildDailyMastermindEmbed() : buildAllTimeMastermindEmbed();
        } else if ("sutom".equals(game)) {
            embed = daily ? buildDailySutomEmbed() : buildAllTimeSutomEmbed();
        } else {
            return null;
        }

        StringSelectMenu menu = StringSelectMenu.create("classement:select")
                .setPlaceholder("Choisis un jeu...")
                .addOption("Sudoku Facile", "sudoku:EASY", "🟢 Classement du Sudoku Facile")
                .addOption("Sudoku Moyen", "sudoku:MEDIUM", "🟡 Classement du Sudoku Moyen")
                .addOption("Sudoku Difficile", "sudoku:HARD", "🔴 Classement du Sudoku Difficile")
                .addOption("Mastermind", "mastermind", "🎯 Classement du Mastermind")
                .addOption("Sutom", "sutom", "🔤 Classement du Sutom")
                .setDefaultValues(List.of(gameKey))
                .build();

        Button dailyBtn = daily
                ? Button.primary("classement:daily:" + gameKey, "📅 Du jour")
                : Button.secondary("classement:daily:" + gameKey, "📅 Du jour");
        Button allTimeBtn = daily
                ? Button.secondary("classement:alltime:" + gameKey, "🏅 Total")
                : Button.primary("classement:alltime:" + gameKey, "🏅 Total");

        return new MessageEditBuilder()
                .setEmbeds(embed.build())
                .setComponents(
                        ActionRow.of(menu),
                        ActionRow.of(dailyBtn, allTimeBtn)
                )
                .build();
    }

    // ---- Sudoku ----

    private EmbedBuilder buildDailySudokuEmbed(SudokuDifficulty difficulty) {
        LocalDate today = LocalDate.now();
        String dateStr = today.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        List<SudokuDatabaseService.ScoreEntry> scores = sudokuDbService.getDailyScores(today, difficulty);

        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle(difficulty.getEmoji() + " Sudoku " + difficulty.getLabel() + " — Classement du jour");
        embed.setColor(new Color(255, 200, 0));

        if (scores.isEmpty()) {
            embed.setDescription("Aucun score pour aujourd'hui. Sois le premier avec `/sudoku` !");
        } else {
            StringBuilder lb = new StringBuilder();
            String[] medals = {"🥇", "🥈", "🥉"};
            for (int i = 0; i < scores.size(); i++) {
                SudokuDatabaseService.ScoreEntry score = scores.get(i);
                String prefix = i < 3 ? medals[i] : (i + 1) + ".";
                lb.append(prefix).append(" <@").append(score.userId()).append("> — **")
                        .append(SudokuGame.formatTime(score.timeSeconds()))
                        .append("** (").append(score.moveCount()).append(" coups)\n");
            }
            embed.setDescription(lb.toString());
        }

        embed.setFooter("📅 " + dateStr + " • " + scores.size() + " participant(s)");
        return embed;
    }

    private EmbedBuilder buildAllTimeSudokuEmbed(SudokuDifficulty difficulty) {
        List<SudokuDatabaseService.AllTimeEntry> scores = sudokuDbService.getAllTimeScores(difficulty);

        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle(difficulty.getEmoji() + " Sudoku " + difficulty.getLabel() + " — Classement total");
        embed.setColor(new Color(200, 150, 255));

        if (scores.isEmpty()) {
            embed.setDescription("Aucun score enregistré. Sois le premier avec `/sudoku` !");
        } else {
            StringBuilder lb = new StringBuilder();
            String[] medals = {"🥇", "🥈", "🥉"};
            for (int i = 0; i < scores.size(); i++) {
                SudokuDatabaseService.AllTimeEntry score = scores.get(i);
                String prefix = i < 3 ? medals[i] : (i + 1) + ".";
                lb.append(prefix).append(" <@").append(score.userId()).append("> — Moy. **")
                        .append(SudokuGame.formatTime(score.avgTimeSeconds()))
                        .append("** | Record **")
                        .append(SudokuGame.formatTime(score.bestTimeSeconds()))
                        .append("** | ").append(score.gamesPlayed()).append(" partie(s)\n");
            }
            embed.setDescription(lb.toString());
        }

        embed.setFooter("Classé par temps moyen • " + scores.size() + " joueur(s)");
        return embed;
    }

    // ---- Mastermind ----

    private EmbedBuilder buildDailyMastermindEmbed() {
        LocalDate today = LocalDate.now();
        String dateStr = today.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        List<MastermindDatabaseService.ScoreEntry> scores = mastermindDbService.getDailyScores(today);

        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("🎯 Mastermind — Classement du jour");
        embed.setColor(new Color(255, 100, 0));

        if (scores.isEmpty()) {
            embed.setDescription("Aucun score pour aujourd'hui. Sois le premier avec `/mastermind` !");
        } else {
            StringBuilder lb = new StringBuilder();
            String[] medals = {"🥇", "🥈", "🥉"};
            for (int i = 0; i < scores.size(); i++) {
                MastermindDatabaseService.ScoreEntry score = scores.get(i);
                String prefix = i < 3 ? medals[i] : (i + 1) + ".";
                lb.append(prefix).append(" <@").append(score.userId()).append("> — **")
                        .append(score.attempts()).append("/").append(MastermindGame.MAX_ATTEMPTS)
                        .append("** en ").append(MastermindGame.formatTime(score.timeSeconds())).append("\n");
            }
            embed.setDescription(lb.toString());
        }

        embed.setFooter("📅 " + dateStr + " • " + scores.size() + " participant(s)");
        return embed;
    }

    private EmbedBuilder buildAllTimeMastermindEmbed() {
        List<MastermindDatabaseService.AllTimeEntry> scores = mastermindDbService.getAllTimeScores();

        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("🎯 Mastermind — Classement total");
        embed.setColor(new Color(255, 150, 50));

        if (scores.isEmpty()) {
            embed.setDescription("Aucun score enregistré. Sois le premier avec `/mastermind` !");
        } else {
            StringBuilder lb = new StringBuilder();
            String[] medals = {"🥇", "🥈", "🥉"};
            for (int i = 0; i < scores.size(); i++) {
                MastermindDatabaseService.AllTimeEntry score = scores.get(i);
                String prefix = i < 3 ? medals[i] : (i + 1) + ".";
                lb.append(prefix).append(" <@").append(score.userId()).append("> — Moy. **")
                        .append(String.format("%.1f", score.avgAttempts())).append("/").append(MastermindGame.MAX_ATTEMPTS)
                        .append("** | Record **").append(score.bestAttempts()).append("/").append(MastermindGame.MAX_ATTEMPTS)
                        .append("** | ").append(score.gamesPlayed()).append(" partie(s)\n");
            }
            embed.setDescription(lb.toString());
        }

        embed.setFooter("Classé par essais moyens • " + scores.size() + " joueur(s)");
        return embed;
    }

    // ---- Sutom ----

    private EmbedBuilder buildDailySutomEmbed() {
        LocalDate today = LocalDate.now();
        String dateStr = today.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        List<SutomDatabaseService.ScoreEntry> scores = sutomDbService.getDailyScores(today);

        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("🔤 Sutom — Classement du jour");
        embed.setColor(new Color(220, 50, 50));

        if (scores.isEmpty()) {
            embed.setDescription("Aucun score pour aujourd'hui. Sois le premier avec `/sutom` !");
        } else {
            StringBuilder lb = new StringBuilder();
            String[] medals = {"🥇", "🥈", "🥉"};
            for (int i = 0; i < scores.size(); i++) {
                SutomDatabaseService.ScoreEntry score = scores.get(i);
                String prefix = i < 3 ? medals[i] : (i + 1) + ".";
                lb.append(prefix).append(" <@").append(score.userId()).append("> — **")
                        .append(score.attempts()).append("/").append(SutomGame.MAX_ATTEMPTS)
                        .append("** en ").append(SutomGame.formatTime(score.timeSeconds())).append("\n");
            }
            embed.setDescription(lb.toString());
        }

        embed.setFooter("📅 " + dateStr + " • " + scores.size() + " participant(s)");
        return embed;
    }

    private EmbedBuilder buildAllTimeSutomEmbed() {
        List<SutomDatabaseService.AllTimeEntry> scores = sutomDbService.getAllTimeScores();

        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("🔤 Sutom — Classement total");
        embed.setColor(new Color(220, 100, 100));

        if (scores.isEmpty()) {
            embed.setDescription("Aucun score enregistré. Sois le premier avec `/sutom` !");
        } else {
            StringBuilder lb = new StringBuilder();
            String[] medals = {"🥇", "🥈", "🥉"};
            for (int i = 0; i < scores.size(); i++) {
                SutomDatabaseService.AllTimeEntry score = scores.get(i);
                String prefix = i < 3 ? medals[i] : (i + 1) + ".";
                lb.append(prefix).append(" <@").append(score.userId()).append("> — Moy. **")
                        .append(String.format("%.1f", score.avgAttempts())).append("/").append(SutomGame.MAX_ATTEMPTS)
                        .append("** | Record **").append(score.bestAttempts()).append("/").append(SutomGame.MAX_ATTEMPTS)
                        .append("** | ").append(score.gamesPlayed()).append(" partie(s)\n");
            }
            embed.setDescription(lb.toString());
        }

        embed.setFooter("Classé par essais moyens • " + scores.size() + " joueur(s)");
        return embed;
    }
}
