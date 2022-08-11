package antifraud.models;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Min;

public class Transaction {

    @NotNull
    @Min(1)
    private Long amount;

    public Long getAmount() {
        return amount;
    }

}
