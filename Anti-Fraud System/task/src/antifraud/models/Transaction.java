package antifraud.models;

import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;
import java.time.LocalDateTime;

@Entity
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long transactionId;

    @NotNull
    @Min(1)
    private Long amount;

    @NotBlank
    @Pattern(regexp = "(([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])")
    private String ip;

    @NotBlank
    private String number;

    @Enumerated(EnumType.STRING)
    private WorldRegionCode region;

    @DateTimeFormat(pattern = "yyyy-MM-ddTHH:mm:ss")
    private LocalDateTime date;

    private TransactionResult result;

    private String feedback = "";

    public Long getAmount() {
        return amount;
    }

    public String getIp() {
        return ip;
    }

    public String getNumber() {
        return number;
    }

    public Long getTransactionId() {
        return transactionId;
    }

    public WorldRegionCode getRegion() {
        return region;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public TransactionResult getResult() {
        return result;
    }

    public void setResult(TransactionResult result) {
        this.result = result;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "id=" + transactionId +
                ", amount=" + amount +
                ", ip='" + ip + '\'' +
                ", number='" + number + '\'' +
                ", region=" + region +
                ", date=" + date +
                '}';
    }
}
