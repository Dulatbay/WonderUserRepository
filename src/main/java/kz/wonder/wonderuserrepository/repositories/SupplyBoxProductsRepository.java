package kz.wonder.wonderuserrepository.repositories;

import kz.wonder.wonderuserrepository.entities.SupplyBoxProducts;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SupplyBoxProductsRepository extends JpaRepository<SupplyBoxProducts, Long> {
    @Query("SELECT s FROM SupplyBoxProducts s WHERE s.product.vendorCode = :productVendorCode and s.product.keycloakId = :keycloakId and s.supplyBox.supply.kaspiStore.id = :kaspiStoreId")
    Optional<SupplyBoxProducts> findByParams(@Param("productVendorCode") String productVendorCode, @Param("keycloakId") String keycloakId, @Param("kaspiStoreId") Long kaspiStoreId);
}
