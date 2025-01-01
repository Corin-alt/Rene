package fr.corentin.rene.commands;

public enum Permission {
    ALL("-1"),
    ADMIN ("562386697174646787");

    private final String roleId;

    Permission(String roleId) {
        this.roleId = roleId;
    }

    public String getRoleId() {
        return roleId;
    }
}
