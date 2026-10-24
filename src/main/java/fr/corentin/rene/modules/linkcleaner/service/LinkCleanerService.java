package fr.corentin.rene.modules.linkcleaner.service;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Reader;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class LinkCleanerService {
    private static final Logger logger = LoggerFactory.getLogger(LinkCleanerService.class);
    private static final int MAX_REDIRECTIONS = 5;

    public static final Pattern URL_PATTERN = Pattern.compile("(https?://[^\\s<]+[^<.,:;\"'>)|\\]\\s])", Pattern.CASE_INSENSITIVE);

    private final List<Provider> providers = new ArrayList<>();

    public LinkCleanerService(Reader rulesReader, boolean removeReferralMarketing) {
        RulesFile file = new Gson().fromJson(rulesReader, RulesFile.class);
        if (file == null || file.providers == null) return;

        for (Map.Entry<String, ProviderData> entry : file.providers.entrySet()) {
            Provider provider = Provider.compile(entry.getKey(), entry.getValue(), removeReferralMarketing);
            if (provider != null) providers.add(provider);
        }
        logger.info("Chargé {} fournisseurs de règles de nettoyage de liens", providers.size());
    }

    public String clean(String url) {
        return clean(url, 0);
    }

    private String clean(String url, int redirections) {
        String current = url;
        for (Provider provider : providers) {
            if (!provider.appliesTo(current)) continue;

            String target = provider.findRedirection(current);
            if (target != null && redirections < MAX_REDIRECTIONS) {
                return clean(target, redirections + 1);
            }
            current = provider.strip(current);
        }
        return current;
    }

    private static Pattern compile(String provider, String regex) {
        try {
            return Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        } catch (PatternSyntaxException e) {
            logger.warn("Règle ignorée pour {} (regex invalide) : {}", provider, regex);
            return null;
        }
    }

    private static List<Pattern> compileAll(String provider, List<String> regexes) {
        List<Pattern> patterns = new ArrayList<>();
        if (regexes == null) return patterns;
        for (String regex : regexes) {
            Pattern pattern = compile(provider, regex);
            if (pattern != null) patterns.add(pattern);
        }
        return patterns;
    }

    private static class RulesFile {
        Map<String, ProviderData> providers;
    }

    private static class ProviderData {
        String urlPattern;
        boolean completeProvider;
        List<String> rules;
        List<String> referralMarketing;
        List<String> rawRules;
        List<String> exceptions;
        List<String> redirections;
    }

    private static class Provider {
        private final Pattern urlPattern;
        private final Pattern paramPattern;
        private final List<Pattern> rawRules;
        private final List<Pattern> exceptions;
        private final List<Pattern> redirections;

        private Provider(Pattern urlPattern, Pattern paramPattern, List<Pattern> rawRules, List<Pattern> exceptions, List<Pattern> redirections) {
            this.urlPattern = urlPattern;
            this.paramPattern = paramPattern;
            this.rawRules = rawRules;
            this.exceptions = exceptions;
            this.redirections = redirections;
        }

        static Provider compile(String name, ProviderData data, boolean removeReferralMarketing) {
            if (data.completeProvider || data.urlPattern == null) return null;

            Pattern urlPattern = LinkCleanerService.compile(name, data.urlPattern);
            if (urlPattern == null) return null;

            List<String> rules = new ArrayList<>();
            if (data.rules != null) rules.addAll(data.rules);
            if (removeReferralMarketing && data.referralMarketing != null) rules.addAll(data.referralMarketing);

            List<String> validRules = new ArrayList<>();
            for (String rule : rules) {
                if (LinkCleanerService.compile(name, rule) != null) validRules.add("(?:" + rule + ")");
            }
            Pattern paramPattern = validRules.isEmpty() ? null
                    : LinkCleanerService.compile(name, "^(?:" + String.join("|", validRules) + ")(?:=[^&]*)?$");

            return new Provider(urlPattern, paramPattern,
                    LinkCleanerService.compileAll(name, data.rawRules),
                    LinkCleanerService.compileAll(name, data.exceptions),
                    LinkCleanerService.compileAll(name, data.redirections));
        }

        boolean appliesTo(String url) {
            if (!urlPattern.matcher(url).find()) return false;
            for (Pattern exception : exceptions) {
                if (exception.matcher(url).find()) return false;
            }
            return true;
        }

        String findRedirection(String url) {
            for (Pattern redirection : redirections) {
                Matcher matcher = redirection.matcher(url);
                if (!matcher.find() || matcher.groupCount() < 1 || matcher.group(1) == null) continue;
                try {
                    String target = URLDecoder.decode(matcher.group(1).replace("+", "%2B"), StandardCharsets.UTF_8);
                    if (target.regionMatches(true, 0, "http://", 0, 7) || target.regionMatches(true, 0, "https://", 0, 8)) {
                        return target;
                    }
                } catch (IllegalArgumentException ignored) {
                }
            }
            return null;
        }

        String strip(String url) {
            String result = url;
            for (Pattern rawRule : rawRules) {
                result = rawRule.matcher(result).replaceAll("");
            }
            if (paramPattern == null) return result;

            int hash = result.indexOf('#');
            String fragment = hash >= 0 ? result.substring(hash + 1) : null;
            String beforeFragment = hash >= 0 ? result.substring(0, hash) : result;
            int question = beforeFragment.indexOf('?');
            String base = question >= 0 ? beforeFragment.substring(0, question) : beforeFragment;
            String query = question >= 0 ? beforeFragment.substring(question + 1) : null;

            String newQuery = query == null ? null : filterParams(query, false);
            String newFragment = fragment == null ? null : filterParams(fragment, true);
            if (Objects.equals(newQuery, query) && Objects.equals(newFragment, fragment)) return result;

            StringBuilder rebuilt = new StringBuilder(base);
            if (newQuery != null && !newQuery.isEmpty()) rebuilt.append('?').append(newQuery);
            if (newFragment != null && !newFragment.isEmpty()) rebuilt.append('#').append(newFragment);
            return rebuilt.toString();
        }

        private String filterParams(String params, boolean keyValueOnly) {
            List<String> kept = new ArrayList<>();
            boolean removed = false;
            for (String param : params.split("&", -1)) {
                boolean candidate = !keyValueOnly || param.contains("=");
                if (candidate && paramPattern.matcher(param).find()) {
                    removed = true;
                } else {
                    kept.add(param);
                }
            }
            return removed ? String.join("&", kept) : params;
        }
    }
}
