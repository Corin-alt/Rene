package fr.corentin.rene.modules.games.mastermind;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MastermindGame {
    public static final int CODE_LENGTH = 4;
    public static final int MAX_ATTEMPTS = 10;

    private final String userId;
    private final String guildChannelId;
    private final LocalDate date;
    private final MastermindColor[] secret;
    private final List<MastermindColor[]> guesses;
    private final List<int[]> feedbacks;
    private final Instant startTime;
    private long dmMessageId;
    private String lastFeedback;
    private boolean completed;
    private boolean failed;

    public MastermindGame(String userId, String guildChannelId, LocalDate date, MastermindColor[] secret) {
        this.userId = userId;
        this.guildChannelId = guildChannelId;
        this.date = date;
        this.secret = secret.clone();
        this.guesses = new ArrayList<>();
        this.feedbacks = new ArrayList<>();
        this.startTime = Instant.now();
        this.lastFeedback = "Bonne chance !";
    }

    public int[] submitGuess(MastermindColor[] guess) {
        int correctPosition = 0;
        int correctColor = 0;
        boolean[] secretUsed = new boolean[CODE_LENGTH];
        boolean[] guessUsed = new boolean[CODE_LENGTH];

        for (int i = 0; i < CODE_LENGTH; i++) {
            if (guess[i] == secret[i]) {
                correctPosition++;
                secretUsed[i] = true;
                guessUsed[i] = true;
            }
        }

        for (int i = 0; i < CODE_LENGTH; i++) {
            if (guessUsed[i]) continue;
            for (int j = 0; j < CODE_LENGTH; j++) {
                if (secretUsed[j]) continue;
                if (guess[i] == secret[j]) {
                    correctColor++;
                    secretUsed[j] = true;
                    break;
                }
            }
        }

        guesses.add(guess.clone());
        int[] feedback = {correctPosition, correctColor};
        feedbacks.add(feedback);

        if (correctPosition == CODE_LENGTH) {
            completed = true;
        } else if (guesses.size() >= MAX_ATTEMPTS) {
            failed = true;
        }

        return feedback;
    }

    public long getElapsedSeconds() {
        return Duration.between(startTime, Instant.now()).getSeconds();
    }

    public String getFormattedTime() {
        return formatTime(getElapsedSeconds());
    }

    public static String formatTime(long totalSeconds) {
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    public int getAttemptCount() { return guesses.size(); }
    public int getRemainingAttempts() { return MAX_ATTEMPTS - guesses.size(); }
    public boolean isCompleted() { return completed; }
    public boolean isFailed() { return failed; }
    public boolean isGameOver() { return completed || failed; }
    public String getUserId() { return userId; }
    public String getGuildChannelId() { return guildChannelId; }
    public LocalDate getDate() { return date; }
    public MastermindColor[] getSecret() { return secret; }
    public List<MastermindColor[]> getGuesses() { return guesses; }
    public List<int[]> getFeedbacks() { return feedbacks; }
    public Instant getStartTime() { return startTime; }
    public long getDmMessageId() { return dmMessageId; }
    public void setDmMessageId(long dmMessageId) { this.dmMessageId = dmMessageId; }
    public String getLastFeedback() { return lastFeedback; }
    public void setLastFeedback(String lastFeedback) { this.lastFeedback = lastFeedback; }
}
