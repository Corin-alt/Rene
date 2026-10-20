package fr.corentin.rene.modules.honeypot.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fr.corentin.rene.Rene;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.requests.ErrorResponse;
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
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class HoneypotService {
    private static final Logger logger = LoggerFactory.getLogger(HoneypotService.class);
    private static final String FILENAME = "honeypot.json";
    private static final Color EMBED_COLOR = new Color(255, 87, 34);

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final Path filePath;
    private HoneypotData data;

    public HoneypotService() {
        this.filePath = new File(Rene.getInstance().getDataFolder(), FILENAME).toPath();
        this.data = load();
    }

    // ---------------------------------------------------------------- Stockage

    private HoneypotData load() {
        if (!Files.exists(filePath)) {
            return new HoneypotData();
        }

        try (Reader reader = Files.newBufferedReader(filePath)) {
            HoneypotData loaded = gson.fromJson(reader, HoneypotData.class);
            if (loaded == null) {
                return new HoneypotData();
            }
            if (loaded.exemptRoleIds == null) {
                loaded.exemptRoleIds = new ArrayList<>();
            }
            return loaded;
        } catch (Exception e) {
            logger.error("Impossible de lire {}, une configuration vide est utilisée (copie dans {}.bak)", FILENAME, FILENAME, e);
            try {
                Files.copy(filePath, filePath.resolveSibling(FILENAME + ".bak"), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException ignored) {
                // Sauvegarde best-effort
            }
            return new HoneypotData();
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

    /**
     * Le piège est actif s'il est activé et que le salon piège est configuré.
     */
    public synchronized boolean isActive() {
        return data.enabled && data.trapChannelId != 0;
    }

    public synchronized boolean isTrapChannel(long channelId) {
        return data.trapChannelId != 0 && data.trapChannelId == channelId;
    }

    public synchronized long getTrapChannelId() {
        return data.trapChannelId;
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

    /**
     * Définit le salon piège. L'ancien embed est supprimé si le salon change, il est reposté par {@link #syncEmbed}.
     */
    public synchronized void setTrapChannel(TextChannel channel) {
        if (data.trapChannelId != channel.getIdLong()) {
            deleteEmbedMessage();
            data.trapChannelId = channel.getIdLong();
            data.embedMessageId = 0;
        }
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

    private synchronized void setEmbedMessageId(long messageId) {
        data.embedMessageId = messageId;
        save();
    }

    // ---------------------------------------------------------------- Embed d'avertissement

    public TextChannel getTrapChannel() {
        long channelId = getTrapChannelId();
        return channelId == 0 ? null : Rene.getInstance().getJda().getTextChannelById(channelId);
    }

    public TextChannel getLogChannel() {
        long channelId = getLogChannelId();
        return channelId == 0 ? null : Rene.getInstance().getJda().getTextChannelById(channelId);
    }

    private MessageEmbed buildEmbed() {
        return new EmbedBuilder()
                .setColor(EMBED_COLOR)
                .setTitle("NE PAS ÉCRIRE DANS CE SALON")
                .setDescription("Ce salon sert à piéger les bots de spam et les comptes piratés. "
                        + "Tout message envoyé ici entraîne un bannissement immédiat.")
                .setFooter("Bans : " + getBanCount())
                .build();
    }

    /**
     * Poste l'embed dans le salon piège, ou le met à jour s'il existe déjà (et le reposte s'il est introuvable).
     *
     * @param done appelé une fois terminé, avec l'erreur éventuelle (null en cas de succès)
     */
    public void syncEmbed(Consumer<Throwable> done) {
        TextChannel channel = getTrapChannel();
        if (channel == null) {
            finish(done, new IllegalStateException("Le salon piège est introuvable."));
            return;
        }

        if (!channel.getGuild().getSelfMember().hasPermission(channel, Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND,
                Permission.MESSAGE_EMBED_LINKS, Permission.MESSAGE_HISTORY)) {
            finish(done, new IllegalStateException("Il me manque des permissions dans " + channel.getAsMention()
                    + " (voir le salon, envoyer des messages, intégrer des liens, voir l'historique)."));
            return;
        }

        MessageEmbed embed = buildEmbed();
        long messageId;
        synchronized (this) {
            messageId = data.embedMessageId;
        }

        if (messageId == 0) {
            postEmbed(channel, embed, done);
            return;
        }

        channel.retrieveMessageById(messageId).queue(
                message -> message.editMessageEmbeds(embed).queue(success -> finish(done, null), error -> finish(done, error)),
                error -> {
                    if (error instanceof ErrorResponseException e && e.getErrorResponse() == ErrorResponse.UNKNOWN_MESSAGE) {
                        postEmbed(channel, embed, done);
                    } else {
                        finish(done, error);
                    }
                });
    }

    private void postEmbed(TextChannel channel, MessageEmbed embed, Consumer<Throwable> done) {
        channel.sendMessageEmbeds(embed).queue(message -> {
            setEmbedMessageId(message.getIdLong());
            finish(done, null);
        }, error -> finish(done, error));
    }

    private void deleteEmbedMessage() {
        if (data.trapChannelId == 0 || data.embedMessageId == 0) {
            return;
        }
        TextChannel oldChannel = Rene.getInstance().getJda().getTextChannelById(data.trapChannelId);
        if (oldChannel != null) {
            oldChannel.deleteMessageById(data.embedMessageId).queue(null, error -> {
                // L'ancien embed est peut-être déjà supprimé ou inaccessible
            });
        }
    }

    private void finish(Consumer<Throwable> done, Throwable error) {
        if (error != null) {
            logger.warn("Honeypot : impossible de mettre à jour l'embed d'avertissement", error);
        }
        if (done != null) {
            done.accept(error);
        }
    }

    // ---------------------------------------------------------------- Logs

    public void logEmbed(MessageEmbed embed) {
        TextChannel logChannel = getLogChannel();
        if (logChannel == null) {
            return;
        }
        try {
            logChannel.sendMessageEmbeds(embed).queue(null,
                    error -> logger.warn("Honeypot : impossible d'écrire dans le salon de logs", error));
        } catch (Exception e) {
            logger.warn("Honeypot : impossible d'écrire dans le salon de logs", e);
        }
    }

    /**
     * Log une erreur dans la console et, si possible, dans le salon de logs.
     */
    public void logError(String message) {
        logger.error("Honeypot : {}", message);
        logEmbed(new EmbedBuilder()
                .setColor(Color.RED)
                .setTitle("Honeypot : erreur")
                .setDescription(message)
                .setTimestamp(Instant.now())
                .build());
    }

    private static class HoneypotData {
        private long trapChannelId;
        private long logChannelId;
        private List<Long> exemptRoleIds = new ArrayList<>();
        private int banCount;
        private boolean enabled = true;
        private long embedMessageId;
    }
}
