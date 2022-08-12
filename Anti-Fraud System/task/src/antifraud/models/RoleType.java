package antifraud.models;

public enum RoleType {
    ROLE_ADMINISTRATOR("ADMINISTRATOR"),
    ROLE_MERCHANT("MERCHANT"),
    ROLE_SUPPORT("SUPPORT");

    private final String roleName;

    RoleType(String r) {
        this.roleName = r;
    }

    public String getRoleName() {
        return roleName;
    }
}
