package fr.corentin.rene.utils;

import net.dv8tion.jda.api.entities.emoji.Emoji;

public class EmojiUtils {

    public static Emoji[] getYesNoEmojis() {
        return new Emoji[] {Emoji.fromUnicode("U+1F44D"), Emoji.fromUnicode("U+1F44E")};
    }

    public static Emoji getEmojiForOption(int index) {
        return Emoji.fromUnicode(switch (index) {
            case 0 -> "U+31 U+FE0F U+20E3";
            case 1 -> "U+32 U+FE0F U+20E3";
            case 2 -> "U+33 U+FE0F U+20E3";
            case 3 -> "U+34 U+FE0F U+20E3";
            case 4 -> "U+35 U+FE0F U+20E3";
            default -> "U+2B50"; // Default emoji for options beyond 5
        });
    }
}
