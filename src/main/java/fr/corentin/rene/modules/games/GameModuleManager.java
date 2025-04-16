package fr.corentin.rene.modules.games;

import fr.corentin.rene.moduleloading.parent.AModule;
import fr.corentin.rene.moduleloading.parent.AModuleConfiguration;

public class GameModuleManager extends AModule {


    public GameModuleManager() {

    }

    @Override
    public void registerCommands() {
    }

    @Override
    public void registerListeners() {
    }

    @Override
    public Class<? extends AModuleConfiguration> getConfigClass() {
        return GameModuleConfiguration.class;
    }
}