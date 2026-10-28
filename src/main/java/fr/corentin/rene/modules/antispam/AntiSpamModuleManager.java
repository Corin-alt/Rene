package fr.corentin.rene.modules.antispam;

import fr.corentin.rene.Rene;
import fr.corentin.rene.managers.CommandManager;
import fr.corentin.rene.managers.EventManager;
import fr.corentin.rene.moduleloading.parent.AModule;
import fr.corentin.rene.moduleloading.parent.AModuleConfiguration;
import fr.corentin.rene.modules.antispam.commands.AntiSpamCommand;
import fr.corentin.rene.modules.antispam.events.AntiSpamMessageListener;
import fr.corentin.rene.modules.antispam.service.AntiSpamService;

import java.util.concurrent.TimeUnit;

public class AntiSpamModuleManager extends AModule {
    private static final long PURGE_INTERVAL_MINUTES = 5;

    private final AntiSpamService antiSpamService;

    public AntiSpamModuleManager() {
        this.antiSpamService = new AntiSpamService();
    }

    @Override
    public void enable() {
        super.enable();

        // Libère régulièrement la mémoire des membres qui n'ont plus écrit depuis la fin de la fenêtre de détection
        Rene.getInstance().getJda().getGatewayPool()
                .scheduleWithFixedDelay(antiSpamService::purge, PURGE_INTERVAL_MINUTES, PURGE_INTERVAL_MINUTES, TimeUnit.MINUTES);
    }

    @Override
    public void registerCommands() {
        CommandManager.getInstance().registerSlashCommand(new AntiSpamCommand(antiSpamService));
    }

    @Override
    public void registerListeners() {
        EventManager.getInstance().registerListener("AntiSpamModule", new AntiSpamMessageListener(antiSpamService));
    }

    @Override
    public Class<? extends AModuleConfiguration> getConfigClass() {
        return AntiSpamModuleConfiguration.class;
    }

    public AntiSpamService getAntiSpamService() {
        return antiSpamService;
    }
}
