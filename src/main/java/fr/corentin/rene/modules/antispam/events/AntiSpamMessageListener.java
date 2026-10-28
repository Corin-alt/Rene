package fr.corentin.rene.modules.antispam.events;

import fr.corentin.rene.events.parent.AEventListener;
import fr.corentin.rene.modules.antispam.service.AntiSpamService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.utils.TimeFormat;
import org.jetbrains.annotations.NotNull;

import java.awt.Color;
import java.time.Instant;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class AntiSpamMessageListener extends AEventListener {
    private static final int BAN_DELETE_DAYS = 7;
    private static final int MAX_CONTENT_LENGTH = 300;
    private static final long RELEASE_DELAY_SECONDS = 60;

    private final AntiSpamService antiSpamService;
    // Utilisateurs dont le bannissement est en cours, pour ne pas bannir deux fois en cas de messages en rafale
    private final Set<Long> banning = ConcurrentHashMap.newKeySet();

    public AntiSpamMessageListener(AntiSpamService antiSpamService) {
        this.antiSpamService = antiSpamService;
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (!event.isFromGuild() || !antiSpamService.isEnabled()) return;
        if (event.getAuthor().isBot() || event.getAuthor().isSystem() || event.isWebhookMessage()) return;

        Member member = event.getMember();
        if (member == null) return;
        if (member.hasPermission(Permission.ADMINISTRATOR) || antiSpamService.isExempt(member)) return;

        User author = event.getAuthor();
        Message message = event.getMessage();
        Guild guild = event.getGuild();

        if (banning.contains(author.getIdLong())) {
            // Bannissement déjà en cours : on supprime juste ce message en plus
            message.delete().queue(null, error -> {});
            return;
        }

        Set<Long> channels = antiSpamService.trackMessage(guild.getIdLong(), author.getIdLong(), event.getChannel().getIdLong());
        if (channels.size() < antiSpamService.getChannelThreshold()) return;

        if (!banning.add(author.getIdLong())) return;

        Member self = guild.getSelfMember();
        String user = author.getName() + " (" + author.getId() + ")";

        if (!self.hasPermission(Permission.BAN_MEMBERS)) {
            fail(event, "Impossible de bannir " + user + " : il me manque la permission **Bannir des membres**.");
            return;
        }
        if (!self.canInteract(member)) {
            fail(event, "Impossible de bannir " + user + " : son rôle le plus haut est supérieur ou égal au mien.");
            return;
        }

        String content = formatContent(message);
        String channelList = channels.stream().map(id -> "<#" + id + ">").collect(Collectors.joining(" "));
        int threshold = antiSpamService.getChannelThreshold();
        int window = antiSpamService.getWindowSeconds();

        guild.ban(author, BAN_DELETE_DAYS, TimeUnit.DAYS)
                .reason("Anti-spam : messages dans " + channels.size() + " salons différents en moins de " + window + " s")
                .queue(success -> onBanned(event, author, content, channelList, channels.size(), threshold, window),
                        error -> fail(event, "Le bannissement de " + user + " a échoué : " + error.getMessage()));
    }

    private void onBanned(MessageReceivedEvent event, User author, String content, String channelList,
                          int channelCount, int threshold, int window) {
        antiSpamService.forget(event.getGuild().getIdLong(), author.getIdLong());

        // Le ban supprime déjà les messages des 7 derniers jours, le message est souvent déjà introuvable
        event.getMessage().delete().queue(null, error -> {});

        int total = antiSpamService.incrementBanCount();

        antiSpamService.logEmbed(new EmbedBuilder()
                .setColor(Color.ORANGE)
                .setTitle("Anti-spam : membre banni")
                .addField("Pseudo", author.getName(), true)
                .addField("ID", author.getId(), true)
                .addField("Date", TimeFormat.DATE_TIME_LONG.format(Instant.now()), false)
                .addField("Détection", channelCount + " salons différents en moins de " + window
                        + " s (seuil : " + threshold + ")", false)
                .addField("Salons", channelList, false)
                .addField("Dernier message", content, false)
                .addField("Total de bans", String.valueOf(total), true)
                .setTimestamp(Instant.now())
                .build());

        release(event, author.getIdLong());
    }

    private void fail(MessageReceivedEvent event, String reason) {
        antiSpamService.logError(reason);
        release(event, event.getAuthor().getIdLong());
    }

    // Garde l'utilisateur en "cours de bannissement" un moment pour éviter de répéter les erreurs à chaque message
    private void release(MessageReceivedEvent event, long userId) {
        event.getJDA().getGatewayPool().schedule(() -> banning.remove(userId), RELEASE_DELAY_SECONDS, TimeUnit.SECONDS);
    }

    private String formatContent(Message message) {
        String text = message.getContentRaw().replace("```", "'''");
        if (text.isBlank()) {
            text = message.getAttachments().isEmpty() ? "(aucun texte)" : "(" + message.getAttachments().size() + " pièce(s) jointe(s))";
        } else if (text.length() > MAX_CONTENT_LENGTH) {
            text = text.substring(0, MAX_CONTENT_LENGTH) + "…";
        }
        return "```" + text + "```";
    }
}
