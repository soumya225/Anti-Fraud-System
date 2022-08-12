package antifraud.models;

import javax.validation.constraints.NotBlank;

public class ChangeRole {

    @NotBlank
    private String username;

    @NotBlank
    private String role;

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }
}
