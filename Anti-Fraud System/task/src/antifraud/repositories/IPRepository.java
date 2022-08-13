package antifraud.repositories;

import antifraud.models.IP;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IPRepository extends CrudRepository<IP, Long> {

    Optional<IP> findIPByIp(String ip);
}
