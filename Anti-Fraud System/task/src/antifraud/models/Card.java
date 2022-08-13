package antifraud.models;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotBlank;

@Entity
public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotBlank
    private String number;

    public long getId() {
        return id;
    }

    public String getNumber() {
        return number;
    }

    //Check validity using Luhn algorithm
    public static boolean isValid(String card) {
        int nDigits = card.length();
        int sum = 0;

        for (int i = 0; i < nDigits - 1; i++) {
            int digit = Integer.parseInt(String.valueOf(card.charAt(i)));
            if (i % 2 == 0) {
                digit = digit * 2;
            }
            if (digit > 9) {
                digit = digit - 9;
            }
            sum += digit;
        }
        sum += Integer.parseInt(String.valueOf(card.charAt(nDigits - 1)));

        return sum % 10 == 0;
    }


}
