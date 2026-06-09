package fr.corentin.rene.modules.games.mastermind;

import java.util.List;

public class MastermindRenderer {

    public static String render(MastermindGame game) {
        List<MastermindColor[]> guesses = game.getGuesses();
        List<int[]> feedbacks = game.getFeedbacks();

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < guesses.size(); i++) {
            sb.append(String.format("`%2d.` ", i + 1));
            MastermindColor[] guess = guesses.get(i);
            for (MastermindColor color : guess) {
                sb.append(color.getEmoji());
            }
            int[] fb = feedbacks.get(i);
            sb.append("  →  ");
            for (int j = 0; j < fb[0]; j++) sb.append("🔴");
            for (int j = 0; j < fb[1]; j++) sb.append("⚪");
            int empty = MastermindGame.CODE_LENGTH - fb[0] - fb[1];
            for (int j = 0; j < empty; j++) sb.append("⚫");
            sb.append("\n");
        }

        if (!game.isGameOver()) {
            sb.append(String.format("`%2d.` ", guesses.size() + 1));
            for (int i = 0; i < MastermindGame.CODE_LENGTH; i++) {
                sb.append("❓");
            }
            sb.append("  ←  prochain essai\n");
        }

        return sb.toString();
    }

    public static String renderSecret(MastermindColor[] secret) {
        StringBuilder sb = new StringBuilder();
        for (MastermindColor color : secret) {
            sb.append(color.getEmoji());
        }
        return sb.toString();
    }
}
