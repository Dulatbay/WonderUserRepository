package kz.wonder.wonderuserrepository.repositories;


import kz.wonder.wonderuserrepository.entities.KaspiOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface KaspiOrderRepository extends JpaRepository<KaspiOrder, Long> {
    Optional<KaspiOrder> findByCode(String code);

    // get orders for seller. todo: remake this method
    Page<KaspiOrder> findAllByWonderUserKeycloakIdAndCreationDateBetween(String keycloakId, Long from, Long to, Pageable pageable);



}
