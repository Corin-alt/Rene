package fr.corentin.rene.modules.games.sutom;

import java.util.List;

public class SutomRenderer {

    public static String render(SutomGame game) {
        List<String> guesses = game.getGuesses();
        List<SutomLetterResult[]> results = game.getResults();

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < guesses.size(); i++) {
            sb.append("`").append(i + 1).append(".` ");
            SutomLetterResult[] result = results.get(i);
            for (SutomLetterResult r : result) {
                sb.append(r.getEmoji());
            }
            sb.append("  `").append(guesses.get(i)).append("`\n");
        }

        if (!game.isGameOver()) {
            sb.append("`").append(guesses.size() + 1).append(".` ");
            sb.append("🟥");
            for (int i = 1; i < game.getWordLength(); i++) {
                sb.append("⬜");
            }
            sb.append("  `").append(game.getFirstLetter());
            for (int i = 1; i < game.getWordLength(); i++) {
                sb.append(" ·");
            }
            sb.append("`\n");
        }

        return sb.toString();
    }
}
