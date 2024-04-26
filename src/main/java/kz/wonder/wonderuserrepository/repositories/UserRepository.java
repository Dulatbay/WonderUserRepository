package kz.wonder.wonderuserrepository.repositories;

import kz.wonder.wonderuserrepository.entities.WonderUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<WonderUser, Long> {
    Optional<WonderUser> findByKeycloakId(String id);

    boolean existsByPhoneNumber(String phoneNumber);
}
