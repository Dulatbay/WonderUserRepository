package kz.wonder.wonderuserrepository.repositories;

import kz.wonder.wonderuserrepository.entities.KaspiToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface KaspiTokenRepository extends JpaRepository<KaspiToken, Long> {
    boolean existsBySellerId(String sellerId);
    boolean existsByToken(String token);

    Optional<KaspiToken> findByWonderUserKeycloakId(String keycloakId);
}
