package fr.corentin.rene.utils;

public enum UserRole {
    MODERATION ("562386697174646787"),
    OUF_MALADE("562387459174825985");

    private String id;

    UserRole(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
