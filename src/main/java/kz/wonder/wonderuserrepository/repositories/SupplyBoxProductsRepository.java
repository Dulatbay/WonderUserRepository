package kz.wonder.wonderuserrepository.repositories;

import kz.wonder.wonderuserrepository.entities.ProductStateInStore;
import kz.wonder.wonderuserrepository.entities.SupplyBoxProduct;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SupplyBoxProductsRepository extends JpaRepository<SupplyBoxProduct, Long> {

    @Query("SELECT sbp FROM SupplyBoxProduct sbp where sbp.product.id = :productId and sbp.supplyBox.supply.kaspiStore.id = :kaspiStoreId and sbp.state = :state")
    List<SupplyBoxProduct> findAllByStoreIdAndProductIdAndState(@Param("kaspiStoreId") Long kaspiStoreId, @Param("productId") Long productId, @Param("state") ProductStateInStore state);

    Optional<SupplyBoxProduct> findByArticle(String productArticle);


    @Query("SELECT sbp FROM SupplyBoxProduct sbp where sbp.article = :article and sbp.supplyBox.supply.kaspiStore.id = :kaspiStoreId")
    Optional<SupplyBoxProduct> findByArticleAndStore(@Param("article") String article, @Param("kaspiStoreId") Long kaspiStoreId);

    @Query(nativeQuery = true, value = "SELECT sbp.* FROM schema_wonder.supply_box_products sbp " +
            "JOIN schema_wonder.kaspi_order_product kop ON kop.supply_box_product_id = sbp.id " +
            "JOIN schema_wonder.kaspi_order ko ON ko.id = kop.kaspi_order_id " +
            "WHERE (ko.creation_date BETWEEN :start AND :end) AND (:productState IS NULL OR sbp.product_state = :productState) AND (:deliveryMode IS NULL OR ko.delivery_mode = :deliveryMode)")
    Page<SupplyBoxProduct> findAllEmployeeResponse(@Param("start") long start, @Param("end") long end, @Param("productState") String productStateInStore, @Param("deliveryMode") String deliveryMode, Pageable pageable);

}
