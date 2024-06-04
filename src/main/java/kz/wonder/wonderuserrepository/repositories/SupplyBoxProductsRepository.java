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
    List<SupplyBoxProduct> findAllByStoreIdAndProductIdAndState(@Param("kaspiStoreId") Long kaspiStoreId, @Param("productId") Long productId, @Param("state") ProductStateInStore state);

    Optional<SupplyBoxProduct> findByArticle(String productArticle);


    @Query("SELECT sbp FROM SupplyBoxProduct sbp where sbp.supplyBox.supply.kaspiStore.wonderUser.keycloakId = :keycloakId and (sbp.state = 'SOLD') and sbp.createdAt BETWEEN :start AND :end")
    List<SupplyBoxProduct> findAllAdminSells(@Param("keycloakId") String keycloakId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT sbp FROM SupplyBoxProduct sbp where sbp.supplyBox.supply.author.keycloakId = :keycloakId and (sbp.state != 'DECLINED') and sbp.createdAt BETWEEN :start AND :end")
    List<SupplyBoxProduct> findAllSellerProducts(@Param("keycloakId") String keycloakId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT sbp FROM SupplyBoxProduct sbp where sbp.article = :article and sbp.supplyBox.supply.kaspiStore.id = :kaspiStoreId")
    Optional<SupplyBoxProduct> findByArticleAndStore(@Param("article") String article, @Param("kaspiStoreId") Long kaspiStoreId);

    @Query("SELECT sbp FROM SupplyBoxProduct sbp " +
            "LEFT JOIN Product p ON p.id = sbp.product.id " +
            "WHERE (:article is null or sbp.article = :article) " +
            "and (:productName is null or p.name = :productName) " +
            "and (:shopName is null or sbp.supplyBox.supply.author.kaspiToken.sellerName = :shopName) " +
            "and (:cellCode is null or sbp.storeCellProduct.storeCell.code = :cellCode) " +
            "and (:vendorCode is null or p.vendorCode = :vendorCode) " +
            "and sbp.supplyBox.supply.kaspiStore.id = :kaspiStoreId")
    Page<SupplyBoxProduct> findByParams(@Param("article") String article,
                                        @Param("productName") String productName,
                                        @Param("shopName") String shopName,
                                        @Param("cellCode") String cellCode,
                                        @Param("vendorCode") String vendorCode,
                                        @Param("kaspiStoreId") Long kaspiStoreId,
                                        Pageable pageable);

    @Query(nativeQuery = true, value = "SELECT sbp.* FROM schema_wonder.supply_box_products sbp " +
            "JOIN schema_wonder.kaspi_order_product kop ON kop.supply_box_product_id = sbp.id " +
            "JOIN schema_wonder.kaspi_order ko ON ko.id = kop.kaspi_order_id " +
            "JOIN schema_wonder.kaspi_store ks ON ks.id = ko.kaspi_store_id " +
            "JOIN schema_wonder.store_employee se ON se.kaspi_store_id  = ks.id " +
            "JOIN schema_wonder.wonder_user wu ON wu.id = se.wonder_user_id " +
            "WHERE (ko.creation_date BETWEEN :start AND :end) AND (:productState IS NULL OR sbp.product_state = :productState) AND (:deliveryMode IS NULL OR ko.delivery_mode = :deliveryMode) AND wu.keycloak_id = :keycloakId")
    Page<SupplyBoxProduct> findAllEmployeeResponse(@Param("start") long start, @Param("end") long end, @Param("productState") String productStateInStore, @Param("deliveryMode") String deliveryMode, @Param("keycloakId") String keycloakId, Pageable pageable);

}
