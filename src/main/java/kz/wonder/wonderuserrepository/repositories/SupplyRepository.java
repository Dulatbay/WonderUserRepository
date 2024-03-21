package kz.wonder.wonderuserrepository.repositories;

import kz.wonder.wonderuserrepository.entities.Supply;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SupplyRepository extends JpaRepository<Supply, Long> {
}
