package antifraud.repositories;

import antifraud.models.Value;
import org.springframework.data.repository.CrudRepository;

public interface ValueRepository extends CrudRepository<Value, String> {
}
