package fr.corentin.rene.modules.birthday.commands;

import fr.corentin.rene.Rene;
import fr.corentin.rene.commands.Permission;
import fr.corentin.rene.commands.parent.APrefixedCommand;
import fr.corentin.rene.modules.birthday.BirthdayModuleManager;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class SendBirthdayCommand extends APrefixedCommand {

    private final BirthdayModuleManager birthdayModuleManager;

    public SendBirthdayCommand(BirthdayModuleManager birthdayModuleManager) {
        super("send-birthday", "Force l'affichage des anniversaires pour ce jour", Permission.ALL);
        this.birthdayModuleManager = birthdayModuleManager;
    }

    @Override
    public boolean execute(MessageReceivedEvent event, String[] args) {
        event.getMessage().delete().queue();
        birthdayModuleManager.getBirthdayScheduler().checkAndSendBirthdayMessages();
        Rene.getInstance().getLogger().info("{} of guild {} forced their birthday announcement in channel {}", event.getAuthor().getEffectiveName(), event.getGuild().getName(), event.getChannel().getName());
        return true;
    }
}
