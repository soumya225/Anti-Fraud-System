package antifraud.models;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Value {

    @Id
    private String key;

    private Integer value;

    public Value(String key, int value) {
        this.key = key;
        this.value = value;
    }

    public Value() { }

    public String getKey() {
        return key;
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }
}
