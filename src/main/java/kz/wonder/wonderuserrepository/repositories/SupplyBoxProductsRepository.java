package kz.wonder.wonderuserrepository.repositories;

import kz.wonder.wonderuserrepository.entities.ProductStateInStore;
import kz.wonder.wonderuserrepository.entities.SupplyBoxProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SupplyBoxProductsRepository extends JpaRepository<SupplyBoxProduct, Long> {
    @Query(value = "SELECT Top 1 s FROM SupplyBoxProduct s " +
            "WHERE s.product.vendorCode = :productVendorCode " +
            "and s.product.keycloakId = :keycloakId " +
            "and s.supplyBox.supply.kaspiStore.id = :kaspiStoreId " +
            "and s.state = :state",
            nativeQuery = true)
    Optional<SupplyBoxProduct> findFirstByParams(@Param("productVendorCode") String productVendorCode, @Param("keycloakId") String keycloakId, @Param("kaspiStoreId") Long kaspiStoreId, @Param("state") ProductStateInStore state);


    Optional<SupplyBoxProduct> findByArticle(String productArticle);
}
