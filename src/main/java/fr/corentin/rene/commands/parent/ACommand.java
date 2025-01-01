package fr.corentin.rene.commands.parent;

import fr.corentin.rene.commands.Permission;
import net.dv8tion.jda.api.entities.Member;

public abstract class ACommand {
    private final String name;
    private final String desc;
    private final Permission permission;

    protected ACommand(String name, String desc, Permission permission) {
        this.name = name;
        this.desc = desc;
        this.permission = permission;
    }

    /**
     * Checks if the member has the required permission to execute the command.
     *
     * @param member The SlashCommandInteractionEvent provided by JDA when the command is invoked.
     * @return true if the member has the required permission, false otherwise.
     */
    public boolean hasPermission(Member member) {
        return !switch (permission) {
            case ALL -> true;
            case ADMIN ->
                // Check if the member has the "ADMIN" role or equivalent.
                    member.hasPermission(net.dv8tion.jda.api.Permission.ADMINISTRATOR);
        };
    }

    public String getName() {
        return name;
    }

    public String getDesc() {
        return desc;
    }

    public Permission getPermission() {
        return permission;
    }
}
