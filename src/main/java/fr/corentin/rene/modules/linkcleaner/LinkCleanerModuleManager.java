package fr.corentin.rene.modules.linkcleaner;

import fr.corentin.rene.managers.EventManager;
import fr.corentin.rene.moduleloading.parent.AModule;
import fr.corentin.rene.moduleloading.parent.AModuleConfiguration;
import fr.corentin.rene.modules.linkcleaner.events.LinkCleanerMessageListener;
import fr.corentin.rene.modules.linkcleaner.service.LinkCleanerService;

public class LinkCleanerModuleManager extends AModule {
    private final LinkCleanerService linkCleanerService;

    public LinkCleanerModuleManager() {
        this.linkCleanerService = new LinkCleanerService();
    }

    @Override
    public void registerCommands() {
    }

    @Override
    public void registerListeners() {
        EventManager.getInstance().registerListener("LinkCleanerModule", new LinkCleanerMessageListener(linkCleanerService));
    }

    @Override
    public Class<? extends AModuleConfiguration> getConfigClass() {
        return LinkCleanerModuleConfiguration.class;
    }
}
