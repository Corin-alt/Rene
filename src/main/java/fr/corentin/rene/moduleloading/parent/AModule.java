package fr.corentin.rene.moduleloading.parent;

import fr.corentin.rene.Rene;

public abstract class AModule {

    protected AModule() {
    }

    public void enable() {
        registerCommands();
        registerListeners();
    }

    public void disable() {
    }

    public abstract void registerCommands();

    public abstract void registerListeners();

    public abstract Class<? extends AModuleConfiguration> getConfigClass();

    protected final AModuleConfiguration getConfig() {
        return Rene.getInstance().getModuleManager().getModulesDatas().get(this.getClass()).getConfiguration();
    }
}
