package fr.corentin.rene.modules.linkcleaner;

import com.google.gson.JsonParseException;
import fr.corentin.rene.managers.EventManager;
import fr.corentin.rene.moduleloading.parent.AModule;
import fr.corentin.rene.moduleloading.parent.AModuleConfiguration;
import fr.corentin.rene.modules.linkcleaner.events.LinkCleanerMessageListener;
import fr.corentin.rene.modules.linkcleaner.service.LinkCleanerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class LinkCleanerModuleManager extends AModule {
    private static final Logger logger = LoggerFactory.getLogger(LinkCleanerModuleManager.class);

    private static final String DEFAULT_RULES_RESOURCE = "/linkcleaner/rules.json";
    private static final String RULES_FILENAME = "rules.json";

    @Override
    public void registerCommands() {
    }

    @Override
    public void registerListeners() {
        LinkCleanerModuleConfiguration config = (LinkCleanerModuleConfiguration) getConfig();
        File rulesFile = new File(config.getDataFolder(), RULES_FILENAME);

        try {
            installDefaultRules(rulesFile);
            LinkCleanerService linkCleanerService;
            try (Reader reader = Files.newBufferedReader(rulesFile.toPath(), StandardCharsets.UTF_8)) {
                linkCleanerService = new LinkCleanerService(reader, config.isRemoveReferralMarketing());
            }
            EventManager.getInstance().registerListener("LinkCleanerModule", new LinkCleanerMessageListener(linkCleanerService));
        } catch (IOException | JsonParseException e) {
            logger.error("Impossible de charger {}, le nettoyage de liens est désactivé", rulesFile, e);
        }
    }

    private void installDefaultRules(File rulesFile) throws IOException {
        if (rulesFile.exists()) return;
        try (InputStream defaults = LinkCleanerModuleManager.class.getResourceAsStream(DEFAULT_RULES_RESOURCE)) {
            if (defaults == null) throw new IOException("Ressource " + DEFAULT_RULES_RESOURCE + " introuvable");
            Files.copy(defaults, rulesFile.toPath());
        }
    }

    @Override
    public Class<? extends AModuleConfiguration> getConfigClass() {
        return LinkCleanerModuleConfiguration.class;
    }
}
