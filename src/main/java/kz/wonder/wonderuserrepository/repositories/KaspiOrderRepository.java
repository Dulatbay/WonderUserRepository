package kz.wonder.wonderuserrepository.repositories;


import kz.wonder.wonderuserrepository.entities.KaspiOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface KaspiOrderRepository extends JpaRepository<KaspiOrder, Long> {
    Optional<KaspiOrder> findByCode(String code);
    List<KaspiOrder> findAllByWonderUserKeycloakId(String keycloakId);
}
