package icu.cykuta.beaconshield.beacon.protection;

public enum PlayerRole {
    OWNER("role-owner", 3),
    OFFICER("role-officer", 2),
    MEMBER("role-member", 1);

    private final String langKey;
    private final int permissionLevel;

    PlayerRole(String langKey, int permissionLevel) {
        this.langKey = langKey;
        this.permissionLevel = permissionLevel;
    }

    public String getLangKey() {
        return langKey;
    }

    public int getPermissionLevel() {
        return permissionLevel;
    }

    /** Devuelve el siguiente rol (circular) */
    public PlayerRole getNext() {
        PlayerRole[] roles = PlayerRole.values();
        int index = (this.ordinal() - 1 + roles.length) % roles.length;
        return roles[index];
    }
}