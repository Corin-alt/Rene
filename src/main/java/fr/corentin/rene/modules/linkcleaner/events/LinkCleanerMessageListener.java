package fr.corentin.rene.modules.linkcleaner.events;

import fr.corentin.rene.events.parent.AEventListener;
import fr.corentin.rene.modules.linkcleaner.service.LinkCleanerService;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;

public class LinkCleanerMessageListener extends AEventListener {
    private static final int MAX_LINKS = 5;
    private static final int MAX_MESSAGE_LENGTH = Message.MAX_CONTENT_LENGTH;

    private final LinkCleanerService linkCleanerService;

    public LinkCleanerMessageListener(LinkCleanerService linkCleanerService) {
        this.linkCleanerService = linkCleanerService;
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (!event.isFromGuild() || event.isWebhookMessage()) return;
        if (event.getAuthor().isBot() || event.getAuthor().isSystem()) return;

        String content = event.getMessage().getContentRaw();
        if (!content.contains("http")) return;

        // Les liens nettoyés, sans doublon et dans l'ordre d'apparition
        Set<String> cleanedLinks = new LinkedHashSet<>();
        Matcher matcher = LinkCleanerService.URL_PATTERN.matcher(content);
        while (matcher.find() && cleanedLinks.size() < MAX_LINKS) {
            String original = matcher.group();
            String cleaned = linkCleanerService.clean(original);
            if (!cleaned.equals(original)) {
                cleanedLinks.add(cleaned);
            }
        }
        if (cleanedLinks.isEmpty() || !event.getGuildChannel().canTalk()) return;

        // Les liens sont entourés de <> pour ne pas dupliquer l'aperçu du message d'origine
        StringBuilder reply = new StringBuilder(cleanedLinks.size() > 1 ? "🧹 Voici les liens sans traqueur :" : "🧹 Voici le lien sans traqueur :");
        for (String link : cleanedLinks) {
            String line = "\n<" + link + ">";
            if (reply.length() + line.length() > MAX_MESSAGE_LENGTH) break;
            reply.append(line);
        }

        event.getMessage().reply(reply.toString())
                .mentionRepliedUser(false)
                .setAllowedMentions(Set.of())
                .queue(null, error -> {});
    }
}
