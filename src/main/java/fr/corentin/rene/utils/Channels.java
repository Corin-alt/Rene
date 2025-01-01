package fr.corentin.rene.utils;

public enum Channels {
    BOT(""),
    LOGS(""),
    GRR(""),
    TAVERNE(""),
    TCHAT_WITH_RENE("");

    private final String channelID;

    Channels(String channelID) {
        this.channelID = channelID;
    }

    public String getChannelID() {
        return channelID;
    }
}
