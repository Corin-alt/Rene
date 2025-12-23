package fr.corentin.rene.modules.moderation;

import fr.corentin.rene.moduleloading.parent.AModuleConfiguration;
import fr.corentin.rene.modules.general.commands.PollCommand;
import fr.corentin.rene.managers.CommandManager;
import fr.corentin.rene.moduleloading.parent.AModule;
import fr.corentin.rene.modules.moderation.commands.GiveawayCommand;

public class ModerationModuleManager extends AModule {

    @Override
    public void registerCommands() {
        CommandManager.getInstance().registerSlashCommand(new GiveawayCommand());
    }

    @Override
    public void registerListeners() {
        //WIP
    }

    @Override
    public Class<? extends AModuleConfiguration> getConfigClass() {
        return ModerationModuleConfiguration.class;
    }
}
