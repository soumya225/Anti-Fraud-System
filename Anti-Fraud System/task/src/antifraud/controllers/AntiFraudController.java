package antifraud.controllers;

import antifraud.models.Card;
import antifraud.models.IP;
import antifraud.models.Transaction;
import antifraud.models.WorldRegionCode;
import antifraud.repositories.CardRepository;
import antifraud.repositories.IPRepository;
import antifraud.repositories.TransactionRepository;
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

    enum TransactionResult {
        ALLOWED,
        MANUAL_PROCESSING,
        PROHIBITED;
    }

    @PostMapping("/transaction")
    public ResponseEntity<Map<String, String>> checkTransaction(@RequestBody @Valid Transaction transaction) {

        TransactionResult result;
        List<String> reasonListManual = new ArrayList<>();
        List<String> reasonListProhibited = new ArrayList<>();

        if(transaction.getAmount() <= 200) {
            result = TransactionResult.ALLOWED;
        } else if (transaction.getAmount() <= 1500) {
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
}
