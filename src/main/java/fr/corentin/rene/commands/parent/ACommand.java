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

    public boolean hasPermission(Member member) {
        return switch (permission) {
            case ALL -> true;
            case ADMIN -> member.hasPermission(net.dv8tion.jda.api.Permission.ADMINISTRATOR);
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
