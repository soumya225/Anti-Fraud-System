package antifraud;

import antifraud.models.UserDetailsImpl;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface UserRepository extends CrudRepository<UserDetailsImpl, Long> {

    Optional<UserDetailsImpl> findUserByUsername(String username);

}
