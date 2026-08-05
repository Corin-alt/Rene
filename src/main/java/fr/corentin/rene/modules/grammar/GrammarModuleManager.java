package fr.corentin.rene.modules.grammar;

import fr.corentin.rene.managers.EventManager;
import fr.corentin.rene.moduleloading.parent.AModule;
import fr.corentin.rene.moduleloading.parent.AModuleConfiguration;
import fr.corentin.rene.modules.grammar.events.GrammarCheckListener;
import fr.corentin.rene.modules.grammar.service.GrammarService;

public class GrammarModuleManager extends AModule {

    private GrammarService grammarService;

    @Override
    public void enable() {
        /**
        this.grammarService = new GrammarService();
         */
        super.enable();
    }

    @Override
    public void registerCommands() {
    }

    @Override
    public void registerListeners() {
        //EventManager.getInstance().registerListener("GrammarModule", new GrammarCheckListener(grammarService));
    }

    @Override
    public Class<? extends AModuleConfiguration> getConfigClass() {
        return GrammarModuleConfiguration.class;
    }
}
