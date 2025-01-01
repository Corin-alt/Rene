package fr.corentin.rene.moduleloading.parent;

import fr.corentin.rene.Rene;
import fr.corentin.rene.commands.parent.ACommand;
import fr.corentin.rene.events.parent.AEventListener;

import java.util.List;
import java.util.Map;

public abstract class AModule {
    private Map<String, ACommand> commands;
    private List<AEventListener> listeners;

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
