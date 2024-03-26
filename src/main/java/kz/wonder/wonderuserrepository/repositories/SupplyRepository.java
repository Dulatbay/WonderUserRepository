package kz.wonder.wonderuserrepository.repositories;

import kz.wonder.wonderuserrepository.entities.Supply;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface SupplyRepository extends JpaRepository<Supply, Long> {
	List<Supply> findAllByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}
