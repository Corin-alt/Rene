package fr.corentin.rene.utils;

public enum Channels {
    LOGS("945784089116553246"),
    GRR("1020025748108628088"),
    TAVERNE("1510965499629146163"),
    BOT("562394254811463707"),
    GAMES("1510965499629146163");

    private final String channelID;

    Channels(String channelID) {
        this.channelID = channelID;
    }

    public String getChannelID() {
        return channelID;
    }
}
