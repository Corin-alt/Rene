package fr.corentin.rene.commands.parent;

import fr.corentin.rene.commands.Permission;
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

import java.util.List;

public abstract class AInteractionCommand extends ACommand {
    private final List<OptionData> options;

    public AInteractionCommand(String name, String desc, Permission permission, List<OptionData> options) {
        super(name, desc, permission);
        this.options = options;
    }

    public abstract boolean execute(GenericCommandInteractionEvent event);

    public List<OptionData> getOptions() {
        return options;
    }

    /**
     * Sous-commandes de la commande slash. Une commande ne peut pas avoir à la fois des options et des sous-commandes.
     */
    public List<SubcommandData> getSubcommands() {
        return List.of();
    }
}
