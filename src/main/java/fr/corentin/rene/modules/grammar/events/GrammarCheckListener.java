package fr.corentin.rene.modules.grammar.events;

import fr.corentin.rene.events.parent.AEventListener;
import fr.corentin.rene.modules.grammar.service.GrammarService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.languagetool.rules.IncorrectExample;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class GrammarCheckListener extends AEventListener {

    private static final Logger logger = LoggerFactory.getLogger(GrammarCheckListener.class);
    private static final String GRAMMAR_EMOJI = "📖";
    private static final String BUTTON_PAGE = "grammar_page_";

    private final GrammarService grammarService;
    // messageId d'origine -> données de grammaire
    private final Map<String, PaginationData> grammarCache = new ConcurrentHashMap<>();
    // messageId du DM -> données de pagination
    private final Map<String, PaginationData> dmCache = new ConcurrentHashMap<>();

    public GrammarCheckListener(GrammarService grammarService) {
        this.grammarService = grammarService;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot() || event.getAuthor().isSystem()) return;
        if (!event.isFromGuild()) return;

        String content = event.getMessage().getContentRaw().trim();
        if (content.isEmpty() || content.startsWith("?") || content.startsWith("/") || content.startsWith("!")) return;
        if (content.length() < 5) return;

        try {
            List<RuleMatch> matches = grammarService.check(content);
            if (matches.isEmpty()) return;

            List<MessageEmbed> pages = buildPages(content, matches);
            if (pages.isEmpty()) return;

            String dataId = event.getMessageId();
            grammarCache.put(dataId, new PaginationData(pages, event.getAuthor().getIdLong()));

            event.getMessage().addReaction(Emoji.fromUnicode(GRAMMAR_EMOJI)).queue();

            event.getMessage().removeReaction(Emoji.fromUnicode(GRAMMAR_EMOJI)).queueAfter(10, TimeUnit.MINUTES,
                    success -> grammarCache.remove(dataId),
                    failure -> grammarCache.remove(dataId));

        } catch (Exception e) {
            logger.error("Erreur lors de la vérification grammaticale", e);
        }
    }

    @Override
    public void onMessageReactionAdd(MessageReactionAddEvent event) {
        if (event.getUser() == null || event.getUser().isBot()) return;
        if (!event.getReaction().getEmoji().getAsReactionCode().equals(GRAMMAR_EMOJI)) return;

        String dataId = event.getMessageId();
        PaginationData data = grammarCache.get(dataId);
        if (data == null) return;
        if (event.getUser().getIdLong() != data.authorId) return;

        event.getChannel().retrieveMessageById(event.getMessageId()).queue(msg -> {
            msg.clearReactions().queue();
        }, failure -> {});

        // Créer une copie pour le DM avec sa propre pagination
        PaginationData dmData = new PaginationData(data.pages, data.authorId);

        event.getUser().openPrivateChannel().queue(dm -> {
            if (dmData.pages.size() == 1) {
                dm.sendMessageEmbeds(dmData.pages.get(0)).queue();
            } else {
                dm.sendMessageEmbeds(dmData.pages.get(0))
                        .setActionRow(buildButtons("temp", 0, dmData.pages.size()))
                        .queue(msg -> {
                            // Remplacer les boutons avec le vrai ID du message DM
                            String dmId = msg.getId();
                            dmCache.put(dmId, dmData);
                            msg.editMessageComponents()
                                    .setActionRow(buildButtons(dmId, 0, dmData.pages.size()))
                                    .queue();
                        });
            }
        }, failure -> logger.debug("Impossible d'ouvrir un DM avec {}", event.getUser().getName()));
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        String componentId = event.getComponentId();
        if (!componentId.startsWith(BUTTON_PAGE)) return;

        String rest = componentId.substring(BUTTON_PAGE.length());
        String action;
        String dmId;

        if (rest.endsWith("_prev")) {
            dmId = rest.substring(0, rest.length() - "_prev".length());
            action = "prev";
        } else if (rest.endsWith("_next")) {
            dmId = rest.substring(0, rest.length() - "_next".length());
            action = "next";
        } else {
            return;
        }

        PaginationData data = dmCache.get(dmId);
        if (data == null) {
            event.reply("Cette correction a expiré.").queue();
            return;
        }

        if ("prev".equals(action) && data.currentPage > 0) {
            data.currentPage--;
        } else if ("next".equals(action) && data.currentPage < data.pages.size() - 1) {
            data.currentPage++;
        }

        event.editMessageEmbeds(data.pages.get(data.currentPage))
                .setActionRow(buildButtons(dmId, data.currentPage, data.pages.size()))
                .queue();
    }

    private List<Button> buildButtons(String dmId, int currentPage, int totalPages) {
        return List.of(
                Button.secondary(BUTTON_PAGE + dmId + "_prev", Emoji.fromUnicode("◀"))
                        .withDisabled(currentPage == 0),
                Button.secondary(BUTTON_PAGE + dmId + "_info", (currentPage + 1) + "/" + totalPages).asDisabled(),
                Button.secondary(BUTTON_PAGE + dmId + "_next", Emoji.fromUnicode("▶"))
                        .withDisabled(currentPage == totalPages - 1)
        );
    }

    private List<MessageEmbed> buildPages(String originalText, List<RuleMatch> matches) {
        List<MessageEmbed> pages = new ArrayList<>();

        for (int i = 0; i < matches.size(); i++) {
            RuleMatch match = matches.get(i);
            Rule rule = match.getRule();

            String errorWord = originalText.substring(match.getFromPos(), match.getToPos());

            EmbedBuilder embed = new EmbedBuilder();
            embed.setColor(new Color(237, 66, 69));
            embed.setTitle("📖 Erreur " + (i + 1) + "/" + matches.size());

            String highlightedText = originalText.substring(0, match.getFromPos())
                    + "**__" + errorWord + "__**"
                    + originalText.substring(match.getToPos());
            embed.setDescription(highlightedText);

            List<String> suggestions = match.getSuggestedReplacements();
            if (!suggestions.isEmpty()) {
                String suggestionsText = String.join(", ", suggestions.subList(0, Math.min(suggestions.size(), 5)));
                embed.addField("✏️ Correction", "「 " + errorWord + " 」 → 「 " + suggestionsText + " 」", false);
            }

            String explication = grammarService.getExplication(rule.getId());
            if (explication != null) {
                String truncated = explication.length() > 1024 ? explication.substring(0, 1021) + "..." : explication;
                embed.addField("💡 Astuce", truncated, false);
            } else {
                embed.addField("💡 Explication", GrammarService.stripHtml(match.getMessage()), false);
            }

            List<IncorrectExample> examples = rule.getIncorrectExamples();
            if (examples != null && !examples.isEmpty()) {
                StringBuilder examplesText = new StringBuilder();
                int exCount = Math.min(examples.size(), 2);
                for (int j = 0; j < exCount; j++) {
                    IncorrectExample ex = examples.get(j);
                    examplesText.append("❌ ").append(GrammarService.stripHtml(ex.getExample()));
                    List<String> corrections = ex.getCorrections();
                    if (corrections != null && !corrections.isEmpty()) {
                        for (String corr : corrections) {
                            if (corr != null && !corr.isEmpty()) {
                                examplesText.append("\n✅ ").append(GrammarService.applyCorrection(ex.getExample(), corr));
                            }
                        }
                    }
                    if (j < exCount - 1) examplesText.append("\n\n");
                }
                String exText = examplesText.toString().trim();
                if (!exText.isEmpty()) {
                    String truncatedEx = exText.length() > 1024 ? exText.substring(0, 1021) + "..." : exText;
                    embed.addField("📝 Exemples", truncatedEx, false);
                }
            }

            pages.add(embed.build());
        }

        return pages;
    }

    private static class PaginationData {
        final List<MessageEmbed> pages;
        final long authorId;
        int currentPage = 0;

        PaginationData(List<MessageEmbed> pages, long authorId) {
            this.pages = pages;
            this.authorId = authorId;
        }
    }
}
