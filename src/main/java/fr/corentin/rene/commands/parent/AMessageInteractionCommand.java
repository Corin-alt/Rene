package fr.corentin.rene.commands.parent;

import fr.corentin.rene.commands.Permission;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.List;

public abstract class AMessageInteractionCommand extends AInteractionCommand {

    public AMessageInteractionCommand(String name, String desc, Permission permission, List<OptionData> options) {
        super(name, desc, permission, options);
    }
}
