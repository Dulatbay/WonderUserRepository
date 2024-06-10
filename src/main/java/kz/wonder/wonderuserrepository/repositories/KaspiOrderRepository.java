package kz.wonder.wonderuserrepository.repositories;


import kz.wonder.wonderuserrepository.entities.DeliveryMode;
import kz.wonder.wonderuserrepository.entities.KaspiOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface KaspiOrderRepository extends JpaRepository<KaspiOrder, Long> {
    Optional<KaspiOrder> findByCode(String code);

    @Query("select ko from KaspiOrder ko " +
            "WHERE ko.wonderUser.keycloakId = :keycloakId " +
            "AND ko.creationDate BETWEEN :from AND :to " +
            "AND (:deliveryMode is null OR ko.deliveryMode = :deliveryMode)")
    Page<KaspiOrder> findAllSellerOrders(String keycloakId, Long from, Long to, DeliveryMode deliveryMode, Pageable pageable);

    @Query("select ko from KaspiOrder ko WHERE ko.wonderUser.keycloakId = :keycloakId AND ko.creationDate BETWEEN :from AND :to")
    List<KaspiOrder> findAllSellerOrders(String keycloakId, Long from, Long to);

    @Query("select ko from KaspiOrder ko " +
            "WHERE ko.kaspiStore.wonderUser.keycloakId = :keycloakId " +
            "AND ko.creationDate BETWEEN :from AND :to " +
            "AND (:deliveryMode is null OR ko.deliveryMode = :deliveryMode) " +
            "ORDER BY ko.creationDate ASC")
    Page<KaspiOrder> findAllAdminOrders(String keycloakId, Long from, Long to, DeliveryMode deliveryMode, Pageable pageable);

    @Query("select ko from KaspiOrder ko WHERE ko.kaspiStore.wonderUser.keycloakId = :keycloakId AND ko.creationDate BETWEEN :from AND :to ORDER BY ko.creationDate ASC")
    List<KaspiOrder> findAllAdminOrders(String keycloakId, Long from, Long to);

    @Query("select ko from KaspiOrder ko " +
            "LEFT JOIN StoreEmployee se ON se.kaspiStore.id = ko.kaspiStore.id " +
            "WHERE se.wonderUser.id = :keycloakId " +
            "AND ko.creationDate BETWEEN :from AND :to " +
            "AND (:deliveryMode is null OR ko.deliveryMode = :deliveryMode) " +
            "ORDER BY ko.creationDate ASC")
    List<KaspiOrder> findAllEmployeeOrders(String keycloakId, Long from, Long to, DeliveryMode deliveryMode);

}
