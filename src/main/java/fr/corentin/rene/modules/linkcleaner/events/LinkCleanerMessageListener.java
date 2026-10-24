package fr.corentin.rene.modules.linkcleaner.events;

import fr.corentin.rene.events.parent.AEventListener;
import fr.corentin.rene.modules.linkcleaner.service.LinkCleanerService;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.unions.GuildMessageChannelUnion;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import net.dv8tion.jda.api.utils.MarkdownSanitizer;
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

        StringBuilder rewritten = new StringBuilder();
        Set<String> cleanedLinks = new LinkedHashSet<>();
        int processed = 0;
        int lastEnd = 0;
        Matcher matcher = LinkCleanerService.URL_PATTERN.matcher(content);
        while (matcher.find() && processed < MAX_LINKS) {
            processed++;
            String original = matcher.group();
            String cleaned = linkCleanerService.clean(original);
            if (cleaned.equals(original)) continue;

            rewritten.append(content, lastEnd, matcher.start()).append(cleaned);
            lastEnd = matcher.end();
            cleanedLinks.add(cleaned);
        }
        if (cleanedLinks.isEmpty()) return;
        rewritten.append(content, lastEnd, content.length());

        GuildMessageChannelUnion channel = event.getGuildChannel();
        if (!channel.canTalk()) return;

        Message original = event.getMessage();
        Member member = event.getMember();
        boolean canReplace = original.getAttachments().isEmpty()
                && original.getStickers().isEmpty()
                && event.getGuild().getSelfMember().hasPermission(channel, Permission.MESSAGE_MANAGE);

        String author = MarkdownSanitizer.escape(member != null ? member.getEffectiveName() : event.getAuthor().getName());
        String text = "Message de " + author + " : " + rewritten;

        if (!canReplace || text.length() > MAX_MESSAGE_LENGTH) {
            replyWithCleanedLinks(original, cleanedLinks);
            return;
        }

        boolean suppressEmbeds = original.getFlags().contains(Message.MessageFlag.EMBEDS_SUPPRESSED)
                || member == null
                || !member.hasPermission(channel, Permission.MESSAGE_EMBED_LINKS);

        MessageCreateAction send = channel.sendMessage(text).setAllowedMentions(Set.of()).setSuppressEmbeds(suppressEmbeds);
        Message referenced = original.getReferencedMessage();
        if (referenced != null) send = send.setMessageReference(referenced).mentionRepliedUser(false);

        send.queue(sent -> original.delete().queue(null, error -> {}), error -> {});
    }

    private void replyWithCleanedLinks(Message original, Set<String> cleanedLinks) {
        StringBuilder reply = new StringBuilder(cleanedLinks.size() > 1 ? "🧹 Voici les liens sans traqueur :" : "🧹 Voici le lien sans traqueur :");
        for (String link : cleanedLinks) {
            String line = "\n<" + link + ">";
            if (reply.length() + line.length() > MAX_MESSAGE_LENGTH) break;
            reply.append(line);
        }

        original.reply(reply.toString())
                .mentionRepliedUser(false)
                .setAllowedMentions(Set.of())
                .queue(null, error -> {});
    }
}
