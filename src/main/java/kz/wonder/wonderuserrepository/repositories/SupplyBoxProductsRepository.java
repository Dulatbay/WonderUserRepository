package kz.wonder.wonderuserrepository.repositories;

import kz.wonder.wonderuserrepository.entities.ProductStateInStore;
import kz.wonder.wonderuserrepository.entities.SupplyBoxProduct;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SupplyBoxProductsRepository extends JpaRepository<SupplyBoxProduct, Long> {

    @Query("SELECT sbp FROM SupplyBoxProduct sbp where sbp.product.id = :productId and sbp.supplyBox.supply.kaspiStore.id = :kaspiStoreId and sbp.state = :state")
    Optional<SupplyBoxProduct> findFirstByStoreIdAndProductIdAndState(@Param("kaspiStoreId") Long kaspiStoreId, @Param("productId") Long productId, @Param("state") ProductStateInStore state);

    Optional<SupplyBoxProduct> findByArticle(String productArticle);


    @Query("SELECT sbp FROM SupplyBoxProduct sbp where sbp.supplyBox.supply.kaspiStore.wonderUser.keycloakId = :keycloakId and (sbp.state = 'SOLD') and sbp.createdAt BETWEEN :start AND :end")
    List<SupplyBoxProduct> findAllAdminSells(@Param("keycloakId") String keycloakId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT sbp FROM SupplyBoxProduct sbp where sbp.supplyBox.supply.author.keycloakId = :keycloakId and (sbp.state != 'DECLINED') and sbp.createdAt BETWEEN :start AND :end")
    List<SupplyBoxProduct> findAllSellerProductsInStore(@Param("keycloakId") String keycloakId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT sbp FROM SupplyBoxProduct sbp where sbp.supplyBox.supply.author.keycloakId = :keycloakId and (sbp.state = 'ACCEPTED')")
    Page<SupplyBoxProduct> findAllSellerProductsInStore(@Param("keycloakId") String keycloakId, Pageable pageable);

    @Query("SELECT sbp FROM SupplyBoxProduct sbp where sbp.article = :article and sbp.supplyBox.supply.kaspiStore.id = :kaspiStoreId")
    Optional<SupplyBoxProduct> findByArticleAndStore(@Param("article") String article, @Param("kaspiStoreId") Long kaspiStoreId);

    @Query("SELECT sbp FROM SupplyBoxProduct sbp " +
            "LEFT JOIN Product p ON p.id = sbp.product.id " +
            "WHERE MIN(sbp.id) IN ( " +
            "  SELECT sbp2.id FROM SupplyBoxProduct sbp2 " +
            "  LEFT JOIN Product p2 ON p2.id = sbp2.product.id " +
            "  WHERE ((:byProductName = false OR lower(p2.name) LIKE '%' || lower(:searchValue) || '%') " +
            "  AND (:byVendorCode = false OR p2.vendorCode LIKE '%' || lower(:searchValue) || '%')) " +
            "  AND sbp2.supplyBox.supply.kaspiStore.id = :kaspiStoreId " +
            "  GROUP BY p2.originalVendorCode, sbp2.id " +
            ")")
    Page<SupplyBoxProduct> findByParams(@Param("kaspiStoreId") Long kaspiStoreId,
                                        @Param("searchValue") String searchValue,
                                        @Param("byProductName") Boolean byProductName,
                                        @Param("byVendorCode") Boolean byVendorCode,
                                        Pageable pageable);



    @Query(nativeQuery = true, value = "SELECT sbp.* FROM schema_wonder.supply_box_products sbp " +
            "JOIN schema_wonder.kaspi_order_product kop ON kop.supply_box_product_id = sbp.id " +
            "JOIN schema_wonder.kaspi_order ko ON ko.id = kop.kaspi_order_id " +
            "JOIN schema_wonder.kaspi_store ks ON ks.id = ko.kaspi_store_id " +
            "JOIN schema_wonder.store_employee se ON se.kaspi_store_id  = ks.id " +
            "JOIN schema_wonder.wonder_user wu ON wu.id = se.wonder_user_id " +
            "WHERE (ko.creation_date BETWEEN :start AND :end) AND (:productState IS NULL OR sbp.product_state = :productState) AND (:deliveryMode IS NULL OR ko.delivery_mode = :deliveryMode) AND wu.keycloak_id = :keycloakId")
    Page<SupplyBoxProduct> findAllEmployeeAssemblies(@Param("start") long start, @Param("end") long end, @Param("productState") String productStateInStore, @Param("deliveryMode") String deliveryMode, @Param("keycloakId") String keycloakId, Pageable pageable);

}
