package antifraud;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping(path = "/api/antifraud")
public class AntiFraudController {

    @PostMapping("/transaction")
    public static ResponseEntity<Map<String, String>> checkTransaction(@RequestBody @Valid Transaction transaction) {

        String result;

        if(transaction.getAmount() <= 200) {
            result = "ALLOWED";
        } else if (transaction.getAmount() <= 1500) {
            result = "MANUAL_PROCESSING";
        } else {
            result = "PROHIBITED";
        }

        Map<String, String> map = new HashMap<>();
        map.put("result", result);

        return new ResponseEntity<>(map, HttpStatus.OK);

    }
}
