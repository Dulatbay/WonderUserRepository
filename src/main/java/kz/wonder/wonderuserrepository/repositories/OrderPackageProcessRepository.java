package kz.wonder.wonderuserrepository.repositories;

import kz.wonder.wonderuserrepository.entities.OrderPackageProcess;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderPackageProcessRepository extends JpaRepository<OrderPackageProcess, Long> {
    Optional<OrderPackageProcess> findBySupplyBoxProductId(Long supplyBoxProductId);
}
