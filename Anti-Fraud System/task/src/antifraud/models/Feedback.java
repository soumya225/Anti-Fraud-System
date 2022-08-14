package antifraud.models;

import javax.validation.constraints.NotNull;

public class Feedback {

    @NotNull
    private Long transactionId;

    private TransactionResult feedback;

    public Long getTransactionId() {
        return transactionId;
    }

    public TransactionResult getFeedback() {
        return feedback;
    }
}
