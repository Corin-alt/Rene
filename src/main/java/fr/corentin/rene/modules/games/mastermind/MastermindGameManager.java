package fr.corentin.rene.modules.games.mastermind;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class MastermindGameManager {
    private final Map<String, MastermindGame> activeGames = new ConcurrentHashMap<>();

    private LocalDate cachedDate;
    private MastermindColor[] cachedSecret;

    public synchronized void ensureDailyCode() {
        LocalDate today = LocalDate.now();
        if (cachedDate != null && today.equals(cachedDate)) return;

        long seed = today.toEpochDay() * 31 + 7919;
        Random random = new Random(seed);

        List<MastermindColor> colors = new ArrayList<>(Arrays.asList(MastermindColor.values()));
        Collections.shuffle(colors, random);

        cachedSecret = new MastermindColor[MastermindGame.CODE_LENGTH];
        for (int i = 0; i < MastermindGame.CODE_LENGTH; i++) {
            cachedSecret[i] = colors.get(i);
        }
        cachedDate = today;
    }

    public MastermindGame startGame(String userId, String channelId) {
        ensureDailyCode();
        MastermindGame game = new MastermindGame(userId, channelId, cachedDate, cachedSecret);
        activeGames.put(userId, game);
        return game;
    }

    public MastermindGame getGame(String userId) {
        return activeGames.get(userId);
    }

    public void removeGame(String userId) {
        activeGames.remove(userId);
    }

    public boolean hasActiveGame(String userId) {
        return activeGames.containsKey(userId);
    }
}
