package antifraud.models;

public enum RoleType {
    ADMINISTRATOR("ROLE_ADMINISTRATOR"),
    MERCHANT("ROLE_MERCHANT"),
    SUPPORT("ROLE_SUPPORT");

    private final String roleName;

    RoleType(String r) {
        this.roleName = r;
    }

    public String getRoleName() {
        return roleName;
    }
}
