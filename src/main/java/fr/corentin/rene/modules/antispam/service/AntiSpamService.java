package fr.corentin.rene.modules.antispam.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fr.corentin.rene.Rene;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class AntiSpamService {
    private static final Logger logger = LoggerFactory.getLogger(AntiSpamService.class);
    private static final String FILENAME = "antispam.json";

    public static final int MIN_CHANNELS = 2;
    public static final int MAX_CHANNELS = 20;
    public static final int MIN_SECONDS = 5;
    public static final int MAX_SECONDS = 600;

    // Garde-fou mémoire : un compte qui spamme ne doit pas faire grossir sa file indéfiniment
    private static final int MAX_TRACKED_MESSAGES = 100;

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final Path filePath;
    private AntiSpamData data;

    // Derniers messages par membre ("guilde:utilisateur"), pour détecter les rafales multi-salons
    private final Map<String, Deque<TrackedMessage>> recentMessages = new ConcurrentHashMap<>();

    public AntiSpamService() {
        this.filePath = new File(Rene.getInstance().getDataFolder(), FILENAME).toPath();
        this.data = load();
    }

    // ---------------------------------------------------------------- Stockage

    private AntiSpamData load() {
        if (!Files.exists(filePath)) {
            return new AntiSpamData();
        }

        try (Reader reader = Files.newBufferedReader(filePath)) {
            AntiSpamData loaded = gson.fromJson(reader, AntiSpamData.class);
            if (loaded == null) {
                return new AntiSpamData();
            }
            if (loaded.exemptRoleIds == null) {
                loaded.exemptRoleIds = new ArrayList<>();
            }
            loaded.channelThreshold = clamp(loaded.channelThreshold, MIN_CHANNELS, MAX_CHANNELS);
            loaded.windowSeconds = clamp(loaded.windowSeconds, MIN_SECONDS, MAX_SECONDS);
            return loaded;
        } catch (Exception e) {
            logger.error("Impossible de lire {}, une configuration par défaut est utilisée (copie dans {}.bak)", FILENAME, FILENAME, e);
            try {
                Files.copy(filePath, filePath.resolveSibling(FILENAME + ".bak"), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException ignored) {
                // Sauvegarde best-effort
            }
            return new AntiSpamData();
        }
    }

    private void save() {
        Path tmp = filePath.resolveSibling(FILENAME + ".tmp");
        try {
            try (Writer writer = Files.newBufferedWriter(tmp)) {
                gson.toJson(data, writer);
            }
            Files.move(tmp, filePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            logger.error("Impossible de sauvegarder {}", FILENAME, e);
        }
    }

    // ---------------------------------------------------------------- Configuration

    public synchronized boolean isEnabled() {
        return data.enabled;
    }

    public synchronized void setEnabled(boolean enabled) {
        data.enabled = enabled;
        save();
    }

    public synchronized int getChannelThreshold() {
        return data.channelThreshold;
    }

    public synchronized int getWindowSeconds() {
        return data.windowSeconds;
    }

    public synchronized void setThreshold(int channels, int seconds) {
        data.channelThreshold = clamp(channels, MIN_CHANNELS, MAX_CHANNELS);
        data.windowSeconds = clamp(seconds, MIN_SECONDS, MAX_SECONDS);
        save();
    }

    public synchronized long getLogChannelId() {
        return data.logChannelId;
    }

    public synchronized int getBanCount() {
        return data.banCount;
    }

    public synchronized List<Long> getExemptRoleIds() {
        return List.copyOf(data.exemptRoleIds);
    }

    public synchronized void setLogChannel(TextChannel channel) {
        data.logChannelId = channel.getIdLong();
        save();
    }

    public synchronized boolean addExemptRole(Role role) {
        if (data.exemptRoleIds.contains(role.getIdLong())) {
            return false;
        }
        data.exemptRoleIds.add(role.getIdLong());
        save();
        return true;
    }

    public synchronized boolean removeExemptRole(Role role) {
        boolean removed = data.exemptRoleIds.remove(Long.valueOf(role.getIdLong()));
        if (removed) {
            save();
        }
        return removed;
    }

    public synchronized boolean isExempt(Member member) {
        return member.getRoles().stream().anyMatch(role -> data.exemptRoleIds.contains(role.getIdLong()));
    }

    public synchronized int incrementBanCount() {
        data.banCount++;
        save();
        return data.banCount;
    }

    // ---------------------------------------------------------------- Détection

    /**
     * Enregistre un message et renvoie les salons distincts où le membre a écrit dans la fenêtre de détection.
     * Le seuil est atteint quand la taille du résultat est supérieure ou égale à {@link #getChannelThreshold()}.
     */
    public Set<Long> trackMessage(long guildId, long userId, long channelId) {
        long now = System.currentTimeMillis();
        long cutoff = now - getWindowSeconds() * 1000L;

        Deque<TrackedMessage> queue = recentMessages.computeIfAbsent(guildId + ":" + userId, key -> new ArrayDeque<>());
        synchronized (queue) {
            prune(queue, cutoff);
            queue.addLast(new TrackedMessage(channelId, now));
            if (queue.size() > MAX_TRACKED_MESSAGES) {
                queue.pollFirst();
            }

            Set<Long> channels = new LinkedHashSet<>();
            queue.forEach(message -> channels.add(message.channelId()));
            return channels;
        }
    }

    public void forget(long guildId, long userId) {
        recentMessages.remove(guildId + ":" + userId);
    }

    /**
     * Retire les membres inactifs de la mémoire (appelé périodiquement).
     */
    public void purge() {
        long cutoff = System.currentTimeMillis() - getWindowSeconds() * 1000L;
        recentMessages.entrySet().removeIf(entry -> {
            Deque<TrackedMessage> queue = entry.getValue();
            synchronized (queue) {
                prune(queue, cutoff);
                return queue.isEmpty();
            }
        });
    }

    // Math.clamp n'existe qu'à partir de Java 21, le runtime est en Java 17
    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static void prune(Deque<TrackedMessage> queue, long cutoff) {
        while (!queue.isEmpty() && queue.peekFirst().timestamp() < cutoff) {
            queue.pollFirst();
        }
    }

    // ---------------------------------------------------------------- Logs

    public TextChannel getLogChannel() {
        long channelId = getLogChannelId();
        return channelId == 0 ? null : Rene.getInstance().getJda().getTextChannelById(channelId);
    }

    public void logEmbed(MessageEmbed embed) {
        TextChannel logChannel = getLogChannel();
        if (logChannel == null) {
            return;
        }
        try {
            logChannel.sendMessageEmbeds(embed).queue(null,
                    error -> logger.warn("AntiSpam : impossible d'écrire dans le salon de logs", error));
        } catch (Exception e) {
            logger.warn("AntiSpam : impossible d'écrire dans le salon de logs", e);
        }
    }

    /**
     * Log une erreur dans la console et, si possible, dans le salon de logs.
     */
    public void logError(String message) {
        logger.error("AntiSpam : {}", message);
        logEmbed(new EmbedBuilder()
                .setColor(Color.RED)
                .setTitle("Anti-spam : erreur")
                .setDescription(message)
                .setTimestamp(Instant.now())
                .build());
    }

    private record TrackedMessage(long channelId, long timestamp) {
    }

    private static class AntiSpamData {
        private boolean enabled = true;
        private int channelThreshold = 4;
        private int windowSeconds = 10;
        private long logChannelId;
        private List<Long> exemptRoleIds = new ArrayList<>();
        private int banCount;
    }
}
