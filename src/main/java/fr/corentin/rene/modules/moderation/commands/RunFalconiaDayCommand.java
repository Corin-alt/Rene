package fr.corentin.rene.modules.moderation.commands;

import fr.corentin.rene.commands.Permission;
import fr.corentin.rene.commands.parent.APrefixedCommand;
import fr.corentin.rene.events.FalconiaDayEventListener;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class RunFalconiaDayCommand extends APrefixedCommand {

    private boolean start = false;

    public RunFalconiaDayCommand() {
        super("run-fd", "Lance le FalconiaDay", Permission.ADMIN);
    }

    @Override
    public boolean execute(MessageReceivedEvent event, String[] args) {
        event.getMessage().delete().queue();
        if (start) return true;
        event.getChannel().sendMessage(FalconiaDayEventListener.MESSAGE).queue();
        start = true;
        return true;
    }
}
