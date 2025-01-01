package fr.corentin.rene.modules.games.tictactoe;

import net.dv8tion.jda.api.entities.emoji.Emoji;

public enum PlayerSymbol {
    X("U+274C", "x"),
    O("U+2B55", "o");

    private final String emoji;
    private final String textEmoji;

    PlayerSymbol(String emoji, String textEmoji) {
        this.emoji = emoji;
        this.textEmoji = textEmoji;
    }

    public Emoji toEmoji() {
        return Emoji.fromUnicode(emoji);
    }

    public String getTextEmoji() {
        return textEmoji;
    }
}