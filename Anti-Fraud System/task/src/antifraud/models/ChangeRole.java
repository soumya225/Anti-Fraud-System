package antifraud.models;

import javax.validation.constraints.NotBlank;

public class ChangeRole {

    @NotBlank
    private String username;

    private RoleType role;

    public String getUsername() {
        return username;
    }

    public RoleType getRole() {
        return role;
    }
}
