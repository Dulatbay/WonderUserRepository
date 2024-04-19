package kz.wonder.wonderuserrepository.repositories;

import kz.wonder.wonderuserrepository.entities.SupplyBoxProducts;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SupplyBoxProductsRepository extends JpaRepository<SupplyBoxProducts, Long> {
    Optional<SupplyBoxProducts> findByProductVendorCodeAndProductKeycloakId(String productVendorCode, String keycloakId);
}
