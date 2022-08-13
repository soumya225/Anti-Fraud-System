package antifraud.repositories;

import antifraud.models.UserDetailsImpl;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends CrudRepository<UserDetailsImpl, Long> {

    Optional<UserDetailsImpl> findUserByUsername(String username);

}
