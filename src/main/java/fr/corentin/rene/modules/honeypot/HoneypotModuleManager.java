package fr.corentin.rene.modules.honeypot;

import fr.corentin.rene.managers.CommandManager;
import fr.corentin.rene.managers.EventManager;
import fr.corentin.rene.moduleloading.parent.AModule;
import fr.corentin.rene.moduleloading.parent.AModuleConfiguration;
import fr.corentin.rene.modules.honeypot.commands.HoneypotCommand;
import fr.corentin.rene.modules.honeypot.events.HoneypotMessageListener;
import fr.corentin.rene.modules.honeypot.service.HoneypotService;

public class HoneypotModuleManager extends AModule {
    private final HoneypotService honeypotService;

    public HoneypotModuleManager() {
        this.honeypotService = new HoneypotService();
    }

    @Override
    public void enable() {
        super.enable();

        // Reposte l'embed d'avertissement s'il a disparu pendant que le bot était arrêté
        if (honeypotService.isActive()) {
            honeypotService.syncEmbed(null);
        }
    }

    @Override
    public void registerCommands() {
        CommandManager.getInstance().registerSlashCommand(new HoneypotCommand(honeypotService));
    }

    @Override
    public void registerListeners() {
        EventManager.getInstance().registerListener("HoneypotModule", new HoneypotMessageListener(honeypotService));
    }

    @Override
    public Class<? extends AModuleConfiguration> getConfigClass() {
        return HoneypotModuleConfiguration.class;
    }

    public HoneypotService getHoneypotService() {
        return honeypotService;
    }
}
