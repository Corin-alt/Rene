package fr.corentin.rene.modules.games.sutom;

public enum SutomLetterResult {
    CORRECT("🟥"),
    PRESENT("🟡"),
    ABSENT("🟦");

    private final String emoji;

    SutomLetterResult(String emoji) {
        this.emoji = emoji;
    }

    public String getEmoji() {
        return emoji;
    }
}
