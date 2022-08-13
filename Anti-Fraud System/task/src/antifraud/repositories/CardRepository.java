package antifraud.repositories;

import antifraud.models.Card;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CardRepository extends CrudRepository<Card, Long>  {

    Optional<Card> findCardByNumber(String number);
}
