package fr.corentin.rene.modules.honeypot.events;

import fr.corentin.rene.events.parent.AEventListener;
import fr.corentin.rene.modules.honeypot.service.HoneypotService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.requests.ErrorResponse;
import net.dv8tion.jda.api.utils.TimeFormat;
import org.jetbrains.annotations.NotNull;

import java.awt.Color;
import java.time.Instant;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class HoneypotMessageListener extends AEventListener {
    private static final int BAN_DELETE_DAYS = 7;
    private static final int MAX_CONTENT_LENGTH = 300;
    private static final long RELEASE_DELAY_SECONDS = 60;

    private final HoneypotService honeypotService;
    // Utilisateurs dont le bannissement est en cours, pour ne pas bannir deux fois en cas de messages en rafale
    private final Set<Long> banning = ConcurrentHashMap.newKeySet();

    public HoneypotMessageListener(HoneypotService honeypotService) {
        this.honeypotService = honeypotService;
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (!event.isFromGuild() || !honeypotService.isActive()) return;
        if (!honeypotService.isTrapChannel(event.getChannel().getIdLong())) return;
        if (event.getAuthor().isBot() || event.getAuthor().isSystem() || event.isWebhookMessage()) return;

        Member member = event.getMember();
        if (member == null) return;
        if (member.hasPermission(Permission.ADMINISTRATOR) || honeypotService.isExempt(member)) return;

        User author = event.getAuthor();
        Message message = event.getMessage();

        if (!banning.add(author.getIdLong())) {
            // Bannissement déjà en cours : il supprimera aussi ce message
            message.delete().queue(null, error -> {});
            return;
        }

        Guild guild = event.getGuild();
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

        guild.ban(author, BAN_DELETE_DAYS, TimeUnit.DAYS)
                .reason("Honeypot : message envoyé dans le salon piège")
                .queue(success -> onBanned(event, author, content), error -> fail(event, "Le bannissement de " + user
                        + " a échoué : " + error.getMessage()));
    }

    private void onBanned(MessageReceivedEvent event, User author, String content) {
        // Le ban supprime déjà les messages des 7 derniers jours, le message est souvent déjà introuvable
        event.getMessage().delete().queue(null, new ErrorHandler()
                .ignore(ErrorResponse.UNKNOWN_MESSAGE)
                .handle(ErrorResponse.MISSING_PERMISSIONS, e -> honeypotService.logError(
                        "Impossible de supprimer le message de " + author.getName() + " : il me manque la permission **Gérer les messages**.")));

        int total = honeypotService.incrementBanCount();
        honeypotService.syncEmbed(null);

        honeypotService.logEmbed(new EmbedBuilder()
                .setColor(Color.ORANGE)
                .setTitle("Honeypot : membre banni")
                .addField("Pseudo", author.getName(), true)
                .addField("ID", author.getId(), true)
                .addField("Date", TimeFormat.DATE_TIME_LONG.format(Instant.now()), false)
                .addField("Message", content, false)
                .addField("Total de bans", String.valueOf(total), true)
                .setTimestamp(Instant.now())
                .build());

        release(event, author.getIdLong());
    }

    private void fail(MessageReceivedEvent event, String reason) {
        honeypotService.logError(reason);
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
