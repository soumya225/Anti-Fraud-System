package antifraud.controllers;

import antifraud.models.*;
import antifraud.repositories.CardRepository;
import antifraud.repositories.IPRepository;
import antifraud.repositories.TransactionRepository;
import antifraud.repositories.ValueRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Pattern;
import java.util.*;

@RestController
@RequestMapping(path = "/api/antifraud")
@Validated
public class AntiFraudController {

    @Autowired
    IPRepository ipRepository;

    @Autowired
    CardRepository cardRepository;

    @Autowired
    TransactionRepository transactionRepository;

    @Autowired
    ValueRepository valueRepository;



    @PostMapping("/transaction")
    public ResponseEntity<Map<String, String>> checkTransaction(@RequestBody @Valid Transaction transaction) {

        TransactionResult result;
        List<String> reasonListManual = new ArrayList<>();
        List<String> reasonListProhibited = new ArrayList<>();


        Optional<Value> maxAllowedOptional = valueRepository.findById("maxAllowed");
        Optional<Value> maxManualOptional = valueRepository.findById("maxManual");

        int maxAllowed = 200;
        int maxManual = 1500;

        if(maxAllowedOptional.isEmpty()) {
            valueRepository.save(new Value ("maxAllowed", maxAllowed));
        } else {
            maxAllowed = maxAllowedOptional.get().getValue();
        }

        if(maxManualOptional.isEmpty()) {
            valueRepository.save(new Value ("maxManual", maxManual));
        } else {
            maxManual = maxManualOptional.get().getValue();
        }

        if(transaction.getAmount() <= maxAllowed) {
            result = TransactionResult.ALLOWED;
        } else if (transaction.getAmount() <= maxManual) {
            result = TransactionResult.MANUAL_PROCESSING;
            reasonListManual.add("amount");
        } else {
            result = TransactionResult.PROHIBITED;
            reasonListProhibited.add("amount");
        }

        Optional<IP> ipOptional = ipRepository.findIPByIp(transaction.getIp());
        Optional<Card> cardOptional = cardRepository.findCardByNumber(transaction.getNumber());

        if(cardOptional.isPresent()) {
            result = TransactionResult.PROHIBITED;
            reasonListProhibited.add("card-number");
        }
        if(ipOptional.isPresent()) {
            result = TransactionResult.PROHIBITED;
            reasonListProhibited.add("ip");
        }

        List<Transaction> transactionListByNumber = transactionRepository.findAllByNumber(transaction.getNumber());

        Set<String> ips = new HashSet<>();
        Set<WorldRegionCode> regions = new HashSet<>();

        ips.add(transaction.getIp());
        regions.add(transaction.getRegion());
        transactionListByNumber.forEach(t -> {
            if(t.getDate().isBefore(transaction.getDate()) &&
                    t.getDate().isAfter(transaction.getDate().minusHours(1))) {
                ips.add(t.getIp());
                regions.add(t.getRegion());
            }
        });


        if(ips.size() > 3) {
            result = TransactionResult.PROHIBITED;
            reasonListProhibited.add("ip-correlation");
        } else if (ips.size() == 3) {
            result = TransactionResult.MANUAL_PROCESSING;
            reasonListManual.add("ip-correlation");
        }

        if(regions.size() > 3) {
            result = TransactionResult.PROHIBITED;
            reasonListProhibited.add("region-correlation");
        } else if (regions.size() == 3) {
            result = TransactionResult.MANUAL_PROCESSING;
            reasonListManual.add("region-correlation");

        }

        transaction.setResult(result);

        transactionRepository.save(transaction);

        String info = "none";
        if(result == TransactionResult.MANUAL_PROCESSING) {
            info = String.join(", ", reasonListManual);
        } else if (result == TransactionResult.PROHIBITED) {
            info = String.join(", ", reasonListProhibited);
        }

        Map<String, String> map = new HashMap<>();
        map.put("result", result.toString());
        map.put("info", info);

        return new ResponseEntity<>(map, HttpStatus.OK);

    }

    @PutMapping("/transaction")
    public ResponseEntity<Transaction> addFeedback(@RequestBody @Valid Feedback feedback) {
        Optional<Transaction> transactionOptional = transactionRepository.findById(feedback.getTransactionId());

        if(transactionOptional.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Transaction transaction = transactionOptional.get();

        if(!transaction.getFeedback().equals("")) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }

        if(feedback.getFeedback().equals(transaction.getResult())) {
            return new ResponseEntity<>(HttpStatus.UNPROCESSABLE_ENTITY);
        }

        Optional<Value> maxAllowedOptional = valueRepository.findById("maxAllowed");
        Optional<Value> maxManualOptional = valueRepository.findById("maxManual");

        Value maxAllowed = maxAllowedOptional.orElse(new Value ("maxAllowed", 200));
        Value maxManual = maxManualOptional.orElse(new Value("maxManual", 1500));


        switch (transaction.getResult()) {
            case ALLOWED:
                switch(feedback.getFeedback()) {
                    case MANUAL_PROCESSING:
                        maxAllowed.setValue(decreaseLimit(maxAllowed.getValue(), transaction.getAmount()));
                        break;
                    case PROHIBITED:
                        maxManual.setValue(decreaseLimit(maxManual.getValue(), transaction.getAmount()));
                        maxAllowed.setValue(decreaseLimit(maxAllowed.getValue(), transaction.getAmount()));
                        break;
                }
                break;
            case MANUAL_PROCESSING:
                switch(feedback.getFeedback()) {
                    case ALLOWED:
                        maxAllowed.setValue(increaseLimit(maxAllowed.getValue(), transaction.getAmount()));
                        break;
                    case PROHIBITED:
                        maxManual.setValue(decreaseLimit(maxManual.getValue(), transaction.getAmount()));
                        break;
                }
                break;
            case PROHIBITED:
                switch (feedback.getFeedback()) {
                    case ALLOWED:
                        maxManual.setValue(increaseLimit(maxManual.getValue(), transaction.getAmount()));
                        maxAllowed.setValue(increaseLimit(maxAllowed.getValue(), transaction.getAmount()));
                        break;
                    case MANUAL_PROCESSING:
                        maxManual.setValue(increaseLimit(maxManual.getValue(), transaction.getAmount()));
                        break;
                }
                break;
        }

        valueRepository.save(maxAllowed);
        valueRepository.save(maxManual);

        transaction.setFeedback(feedback.getFeedback().toString());
        transactionRepository.save(transaction);

        return new ResponseEntity<>(transaction, HttpStatus.OK);
    }

    @GetMapping("/history")
    public ResponseEntity<List<Transaction>> getHistory() {
        List<Transaction> transactionList = new ArrayList<>();

        transactionRepository.findAll().forEach(transactionList::add);

        return new ResponseEntity<>(transactionList, HttpStatus.OK);
    }

    @GetMapping("/history/{number}")
    public ResponseEntity<List<Transaction>> getHistoryByNumber(@PathVariable String number) {
        if(!Card.isValid(number)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        List<Transaction> transactionList = transactionRepository.findAllByNumber(number);

        if(transactionList.isEmpty()){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(transactionList, HttpStatus.OK);
    }

    @PostMapping("/suspicious-ip")
    public ResponseEntity<IP> saveSuspiciousIP(@RequestBody @Valid IP ip) {

        Optional<IP> ipOptional = ipRepository.findIPByIp(ip.getIp());

        if(ipOptional.isPresent()) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }

        ipRepository.save(ip);

        return new ResponseEntity<>(ip, HttpStatus.OK);
    }

    @DeleteMapping("/suspicious-ip/{ip}")
    public ResponseEntity<Map<String, String>> deleteIP(
            @PathVariable @Pattern(regexp = "(([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])")
            String ip) {

        Optional<IP> ipOptional = ipRepository.findIPByIp(ip);

        if(ipOptional.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        ipRepository.delete(ipOptional.get());

        Map<String, String> map = new HashMap<>();
        map.put("status", "IP " + ip + " successfully removed!");

        return new ResponseEntity<>(map, HttpStatus.OK);
    }

    @GetMapping("/suspicious-ip")
    public ResponseEntity<List<IP>> getSuspiciousIPs() {
        List<IP> ipList = new ArrayList<>();
        ipRepository.findAll().forEach(ipList::add);

        return new ResponseEntity<>(ipList, HttpStatus.OK);
    }

    @PostMapping("/stolencard")
    public ResponseEntity<Card> saveStolenCard(@RequestBody @Valid Card card) {

        Optional<Card> cardOptional = cardRepository.findCardByNumber(card.getNumber());

        if(cardOptional.isPresent()) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }

        if(!Card.isValid(card.getNumber())) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        cardRepository.save(card);

        return new ResponseEntity<>(card, HttpStatus.OK);
    }

    @DeleteMapping("/stolencard/{number}")
    public ResponseEntity<Map<String, String>> deleteCard(@PathVariable String number) {

        if(!Card.isValid(number)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        Optional<Card> cardOptional = cardRepository.findCardByNumber(number);

        if(cardOptional.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        cardRepository.delete(cardOptional.get());

        Map<String, String> map = new HashMap<>();
        map.put("status", "Card " + number + " successfully removed!");

        return new ResponseEntity<>(map, HttpStatus.OK);
    }

    @GetMapping("/stolencard")
    public ResponseEntity<List<Card>> getStolenCards() {
        List<Card> cardList = new ArrayList<>();
        cardRepository.findAll().forEach(cardList::add);

        return new ResponseEntity<>(cardList, HttpStatus.OK);
    }

    private int increaseLimit(int limit, long valueFromTransaction) {
        return (int) Math.ceil(0.8 * limit + 0.2 * valueFromTransaction);
    }

    private int decreaseLimit(int limit, long valueFromTransaction) {
        return (int) Math.ceil(0.8 * limit - 0.2 * valueFromTransaction);
    }
}
