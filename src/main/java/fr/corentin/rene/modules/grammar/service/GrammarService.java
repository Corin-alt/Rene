package fr.corentin.rene.modules.grammar.service;

import org.languagetool.JLanguageTool;
import org.languagetool.Languages;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GrammarService {

    private static final Logger logger = LoggerFactory.getLogger(GrammarService.class);
    private static final Pattern MARKER_PATTERN = Pattern.compile("<marker>(.*?)</marker>");
    private static final Pattern DISCORD_EMOJI_PATTERN = Pattern.compile("<a?:\\w+:\\d+>");
    private static final Pattern UNICODE_EMOJI_PATTERN = Pattern.compile(
            "[\\x{1F600}-\\x{1F64F}\\x{1F300}-\\x{1F5FF}\\x{1F680}-\\x{1F6FF}\\x{1F900}-\\x{1F9FF}" +
            "\\x{1FA00}-\\x{1FA6F}\\x{1FA70}-\\x{1FAFF}\\x{2600}-\\x{26FF}\\x{2700}-\\x{27BF}" +
            "\\x{FE00}-\\x{FE0F}\\x{200D}\\x{20E3}\\x{E0020}-\\x{E007F}" +
            "\\x{2300}-\\x{23FF}\\x{2B50}\\x{2B55}\\x{3030}\\x{303D}\\x{3297}\\x{3299}]+");
    private static final Pattern PUNCTUATION_ONLY_PATTERN = Pattern.compile("^[\\p{Punct}\\s,:;.!?…·•–—''\"«»]+$");

    private final JLanguageTool langTool;
    private final Map<String, String> explications = new HashMap<>();

    private static final List<String> IGNORED_CATEGORIES = List.of(
            "PUNCTUATION", "TYPOGRAPHY", "WHITESPACE", "CASING", "REDUNDANCY"
    );

    private static final List<String> IGNORED_RULE_IDS = List.of(
            "UPPERCASE_SENTENCE_START",
            "COMMA_PARENTHESIS_WHITESPACE",
            "DOUBLE_PUNCTUATION",
            "UNPAIRED_BRACKETS",
            "WHITESPACE_RULE",
            "SENTENCE_WHITESPACE",
            "FR_SPELLING_RULE"
    );

    public GrammarService() {
        this.langTool = new JLanguageTool(Languages.getLanguageForShortCode("fr"));
        disableIgnoredRules();
        loadExplications();
    }

    private void disableIgnoredRules() {
        List<Rule> allRules = langTool.getAllRules();
        for (Rule rule : allRules) {
            String categoryId = rule.getCategory().getId().toString();
            String ruleId = rule.getId();

            if (IGNORED_CATEGORIES.stream().anyMatch(categoryId::contains)
                    || IGNORED_RULE_IDS.contains(ruleId)) {
                langTool.disableRule(ruleId);
            }
        }
        logger.info("Règles de ponctuation, espaces et typographie désactivées");
    }

    private void loadExplications() {
        try (InputStream is = getClass().getResourceAsStream("/regles.json")) {
            if (is == null) {
                logger.warn("regles.json introuvable dans les ressources");
                return;
            }
            String json = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            parseReglesJson(json);
            logger.info("Chargé {} règles de grammaire depuis regles.json", explications.size());
        } catch (IOException e) {
            logger.error("Erreur lors du chargement de regles.json", e);
        }
    }

    private void parseReglesJson(String json) {
        json = json.trim();
        if (json.startsWith("{")) json = json.substring(1);
        if (json.endsWith("}")) json = json.substring(0, json.length() - 1);

        Pattern keyPattern = Pattern.compile("\"([^\"]+)\"\\s*:\\s*\\[");
        Matcher keyMatcher = keyPattern.matcher(json);

        while (keyMatcher.find()) {
            String key = keyMatcher.group(1);
            int arrayStart = keyMatcher.end();
            int arrayEnd = json.indexOf(']', arrayStart);
            if (arrayEnd == -1) continue;

            String arrayContent = json.substring(arrayStart, arrayEnd);
            Pattern stringPattern = Pattern.compile("\"((?:[^\"\\\\]|\\\\.)*)\"");
            Matcher stringMatcher = stringPattern.matcher(arrayContent);

            StringBuilder explanation = new StringBuilder();
            while (stringMatcher.find()) {
                if (explanation.length() > 0) explanation.append("\n");
                String line = stringMatcher.group(1)
                        .replace("\\\"", "\"")
                        .replace("\\n", "\n")
                        .replace("\\\\", "\\");
                explanation.append(line);
            }

            if (explanation.length() > 0) {
                explications.put(key, explanation.toString());
            }
        }
    }

    public String stripEmojis(String text) {
        text = replaceKeepingLength(DISCORD_EMOJI_PATTERN, text);
        text = replaceKeepingLength(UNICODE_EMOJI_PATTERN, text);
        return text;
    }

    private String replaceKeepingLength(Pattern pattern, String text) {
        Matcher m = pattern.matcher(text);
        StringBuilder sb = new StringBuilder();
        while (m.find()) {
            m.appendReplacement(sb, Matcher.quoteReplacement(" ".repeat(m.group().length())));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    public List<RuleMatch> check(String text) throws IOException {
        String cleanedText = stripEmojis(text);
        List<RuleMatch> matches = langTool.check(cleanedText);
        return matches.stream()
                .filter(match -> {
                    String matched = cleanedText.substring(match.getFromPos(), match.getToPos());
                    if (PUNCTUATION_ONLY_PATTERN.matcher(matched).matches()) {
                        return false;
                    }
                    String categoryId = match.getRule().getCategory().getId().toString();
                    return IGNORED_CATEGORIES.stream().noneMatch(categoryId::contains);
                })
                .collect(Collectors.toList());
    }

    public String getExplication(String ruleId) {
        if (explications.containsKey(ruleId)) {
            return explications.get(ruleId);
        }
        for (Map.Entry<String, String> entry : explications.entrySet()) {
            if (ruleId.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }

    public static String stripHtml(String text) {
        if (text == null) return "";
        return text.replaceAll("<[^>]+>", "");
    }

    public static String applyCorrection(String exampleWithMarker, String correction) {
        Matcher m = MARKER_PATTERN.matcher(exampleWithMarker);
        if (m.find()) {
            return stripHtml(exampleWithMarker.substring(0, m.start()) + correction + exampleWithMarker.substring(m.end()));
        }
        return stripHtml(exampleWithMarker);
    }
}
