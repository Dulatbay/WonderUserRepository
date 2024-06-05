package kz.wonder.wonderuserrepository.repositories;

import kz.wonder.wonderuserrepository.entities.OrderAssembleProcess;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderAssembleProcessRepository extends JpaRepository<OrderAssembleProcess, Long> {
    Optional<OrderAssembleProcess> findByOrderAssembleIdAndStoreCellProductId(Long assembleId, Long storeCellProductId);
}
