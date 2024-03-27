package kz.wonder.wonderuserrepository.repositories;

import kz.wonder.wonderuserrepository.entities.Supply;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SupplyRepository extends JpaRepository<Supply, Long> {
	List<Supply> findAllByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
	List<Supply> findAllByCreatedAtBetweenAndAuthorKeycloakId(LocalDateTime start, LocalDateTime end, String keycloakId);

	Optional<Supply> findByIdAndAuthorKeycloakId(Long id, String keycloakId);
}
