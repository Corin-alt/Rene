package fr.corentin.rene.modules.games.mastermind;

public enum MastermindColor {
    ROUGE("🔴", 'R', "Rouge"),
    BLEU("🔵", 'B', "Bleu"),
    VERT("🟢", 'V', "Vert"),
    JAUNE("🟡", 'J', "Jaune"),
    VIOLET("🟣", 'P', "Violet"),
    ORANGE("🟠", 'O', "Orange");

    private final String emoji;
    private final char code;
    private final String label;

    MastermindColor(String emoji, char code, String label) {
        this.emoji = emoji;
        this.code = code;
        this.label = label;
    }

    public String getEmoji() { return emoji; }
    public char getCode() { return code; }
    public String getLabel() { return label; }

    public static MastermindColor fromCode(char code) {
        for (MastermindColor c : values()) {
            if (c.code == Character.toUpperCase(code)) return c;
        }
        return null;
    }

    public static String allCodesDisplay() {
        StringBuilder sb = new StringBuilder();
        for (MastermindColor c : values()) {
            if (sb.length() > 0) sb.append("  ");
            sb.append(c.emoji).append(" **").append(c.code).append("** (").append(c.label).append(")");
        }
        return sb.toString();
    }
}
