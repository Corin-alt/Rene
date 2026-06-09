package fr.corentin.rene.modules.games.sutom;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class SutomGame {
    public static final int MAX_ATTEMPTS = 6;

    private final String userId;
    private final String guildChannelId;
    private final LocalDate date;
    private final String targetWord;
    private final List<String> guesses;
    private final List<SutomLetterResult[]> results;
    private final Instant startTime;
    private long dmMessageId;
    private String lastFeedback;
    private boolean completed;
    private boolean failed;

    public SutomGame(String userId, String guildChannelId, LocalDate date, String targetWord) {
        this.userId = userId;
        this.guildChannelId = guildChannelId;
        this.date = date;
        this.targetWord = targetWord.toUpperCase();
        this.guesses = new ArrayList<>();
        this.results = new ArrayList<>();
        this.startTime = Instant.now();
        this.lastFeedback = "Bonne chance !";
    }

    public SutomLetterResult[] submitGuess(String guess) {
        guess = guess.toUpperCase();
        SutomLetterResult[] result = evaluate(guess, targetWord);
        guesses.add(guess);
        results.add(result);

        boolean allCorrect = true;
        for (SutomLetterResult r : result) {
            if (r != SutomLetterResult.CORRECT) {
                allCorrect = false;
                break;
            }
        }

        if (allCorrect) {
            completed = true;
        } else if (guesses.size() >= MAX_ATTEMPTS) {
            failed = true;
        }

        return result;
    }

    static SutomLetterResult[] evaluate(String guess, String target) {
        int len = target.length();
        SutomLetterResult[] result = new SutomLetterResult[len];
        boolean[] targetUsed = new boolean[len];
        boolean[] guessUsed = new boolean[len];

        for (int i = 0; i < len; i++) {
            if (guess.charAt(i) == target.charAt(i)) {
                result[i] = SutomLetterResult.CORRECT;
                targetUsed[i] = true;
                guessUsed[i] = true;
            }
        }

        for (int i = 0; i < len; i++) {
            if (guessUsed[i]) continue;
            boolean found = false;
            for (int j = 0; j < len; j++) {
                if (targetUsed[j]) continue;
                if (guess.charAt(i) == target.charAt(j)) {
                    result[i] = SutomLetterResult.PRESENT;
                    targetUsed[j] = true;
                    found = true;
                    break;
                }
            }
            if (!found) {
                result[i] = SutomLetterResult.ABSENT;
            }
        }

        return result;
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

    public char getFirstLetter() { return targetWord.charAt(0); }
    public int getWordLength() { return targetWord.length(); }
    public int getAttemptCount() { return guesses.size(); }
    public int getRemainingAttempts() { return MAX_ATTEMPTS - guesses.size(); }
    public boolean isCompleted() { return completed; }
    public boolean isFailed() { return failed; }
    public boolean isGameOver() { return completed || failed; }
    public String getUserId() { return userId; }
    public String getGuildChannelId() { return guildChannelId; }
    public LocalDate getDate() { return date; }
    public String getTargetWord() { return targetWord; }
    public List<String> getGuesses() { return guesses; }
    public List<SutomLetterResult[]> getResults() { return results; }
    public Instant getStartTime() { return startTime; }
    public long getDmMessageId() { return dmMessageId; }
    public void setDmMessageId(long dmMessageId) { this.dmMessageId = dmMessageId; }
    public String getLastFeedback() { return lastFeedback; }
    public void setLastFeedback(String lastFeedback) { this.lastFeedback = lastFeedback; }
}
