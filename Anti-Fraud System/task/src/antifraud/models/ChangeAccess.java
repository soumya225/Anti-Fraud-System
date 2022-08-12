package antifraud.models;

import javax.validation.constraints.NotBlank;

public class ChangeAccess {

    @NotBlank
    private String username;

    @NotBlank
    private String operation;

    public String getUsername() {
        return username;
    }

    public String getOperation() {
        return operation;
    }

}
