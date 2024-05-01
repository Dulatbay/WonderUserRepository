package kz.wonder.wonderuserrepository.repositories;

import kz.wonder.wonderuserrepository.entities.SupplyBoxProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SupplyBoxProductsRepository extends JpaRepository<SupplyBoxProduct, Long> {

    @Query("SELECT sbp FROM SupplyBoxProduct sbp where sbp.product.id = :productId and sbp.supplyBox.supply.kaspiStore.id = :kaspiStoreId")
    List<SupplyBoxProduct> findAllByStoreIdAndProductId(@Param("kaspiStoreId") Long kaspiStoreId, @Param("productId") Long productId);

    Optional<SupplyBoxProduct> findByArticle(String productArticle);

}
