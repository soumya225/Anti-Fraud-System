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
    private Long id;

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

//    @Pattern(regexp = "\\d{4}-(0[1-9]|1[0-2])-(0[1-9]|[12][0-9]|3[01])T(?:(?:([01]?\\d|2[0-3]):)?([0-5]?\\d):)?([0-5]?\\d)")
    @DateTimeFormat(pattern = "yyyy-MM-ddTHH:mm:ss")
    private LocalDateTime date;

    public Long getAmount() {
        return amount;
    }

    public String getIp() {
        return ip;
    }

    public String getNumber() {
        return number;
    }

    public Long getId() {
        return id;
    }

    public WorldRegionCode getRegion() {
        return region;
    }

    public LocalDateTime getDate() {
        return date;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "id=" + id +
                ", amount=" + amount +
                ", ip='" + ip + '\'' +
                ", number='" + number + '\'' +
                ", region=" + region +
                ", date=" + date +
                '}';
    }
}
