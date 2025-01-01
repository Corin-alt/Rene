package fr.corentin.rene.modules.birthday;

import fr.corentin.rene.managers.CommandManager;
import fr.corentin.rene.managers.EventManager;
import fr.corentin.rene.moduleloading.parent.AModule;
import fr.corentin.rene.modules.birthday.commands.SendBirthdayCommand;
import fr.corentin.rene.modules.birthday.events.BirthdayModalListener;
import fr.corentin.rene.modules.birthday.service.BirthdayDatabaseService;
import fr.corentin.rene.modules.birthday.service.BirthdaySchedulerService;
import fr.corentin.rene.modules.birthday.commands.BirthdayCommand;

public class BirthdayModuleManager extends AModule {
    private final BirthdaySchedulerService birthdaySchedulerService;
    private final BirthdayDatabaseService birthdayDatabaseService;

    public BirthdayModuleManager() {
        this.birthdayDatabaseService = new BirthdayDatabaseService();
        this.birthdaySchedulerService = new BirthdaySchedulerService(this);
    }

    @Override
    public void registerCommands() {
        CommandManager.getInstance().registerSlashCommand(new BirthdayCommand());
        CommandManager.getInstance().registerPrefixedCommand(new SendBirthdayCommand(this));
    }

    @Override
    public void registerListeners() {
        EventManager.getInstance().registerListener("BirthdayModule", new BirthdayModalListener(this.birthdayDatabaseService));
    }

    @Override
    public Class<BirthdayModuleConfiguration> getConfigClass() {
        return BirthdayModuleConfiguration.class;
    }

    public BirthdaySchedulerService getBirthdayScheduler() {
        return birthdaySchedulerService;
    }

    public BirthdayDatabaseService getBirthdayDatabaseService() {
        return birthdayDatabaseService;
    }
}
