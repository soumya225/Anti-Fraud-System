package antifraud.models;

import javax.validation.constraints.NotBlank;

public class ChangeAccess {

    @NotBlank
    private String username;

    private Operation operation;

    public String getUsername() {
        return username;
    }

    public Operation getOperation() {
        return operation;
    }

}
