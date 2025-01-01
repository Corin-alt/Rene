package fr.corentin.rene.commands.parent;

import fr.corentin.rene.commands.Permission;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public abstract class APrefixedCommand extends ACommand {

    protected APrefixedCommand(String name, String desc, Permission permission) {
        super(name, desc, permission);
    }

    public abstract boolean execute(MessageReceivedEvent event, String[] args);
}
