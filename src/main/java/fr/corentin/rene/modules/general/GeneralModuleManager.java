package fr.corentin.rene.modules.general;

import fr.corentin.rene.managers.CommandManager;
import fr.corentin.rene.moduleloading.parent.AModule;
import fr.corentin.rene.moduleloading.parent.AModuleConfiguration;
import fr.corentin.rene.modules.general.commands.*;

public class GeneralModuleManager extends AModule {

    @Override
    public void registerCommands() {
        CommandManager.getInstance().registerSlashCommand(new HelpCommand());
        CommandManager.getInstance().registerSlashCommand(new HeisenbergCommand());
        CommandManager.getInstance().registerSlashCommand(new PingCommand());
        CommandManager.getInstance().registerSlashCommand(new UptimeCommand());
        CommandManager.getInstance().registerSlashCommand(new PollCommand());
        CommandManager.getInstance().registerUserContextCommand(new AvatarCommand());
        CommandManager.getInstance().registerMessageContextCommand(new PinCommand());
        CommandManager.getInstance().registerMessageContextCommand(new UnpinCommand());

    }

    @Override
    public void registerListeners() {

    }

    @Override
    public Class<? extends AModuleConfiguration> getConfigClass() {
        return GeneralModuleConfiguration.class;
    }
}
