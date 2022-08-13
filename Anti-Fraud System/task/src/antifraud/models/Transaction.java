package antifraud.models;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;

public class Transaction {

    @NotNull
    @Min(1)
    private Long amount;

    @NotBlank
    @Pattern(regexp = "(([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])")
    private String ip;
    private String number;

    public Long getAmount() {
        return amount;
    }

    public String getIp() {
        return ip;
    }

    public String getNumber() {
        return number;
    }
}
