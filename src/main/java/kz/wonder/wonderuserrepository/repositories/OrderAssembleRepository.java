package kz.wonder.wonderuserrepository.repositories;

import kz.wonder.wonderuserrepository.entities.OrderAssemble;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderAssembleRepository extends JpaRepository<OrderAssemble, Long> {
}
