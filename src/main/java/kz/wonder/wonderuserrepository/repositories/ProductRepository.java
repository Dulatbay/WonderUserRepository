package kz.wonder.wonderuserrepository.repositories;

import kz.wonder.wonderuserrepository.entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findByVendorCodeAndKeycloakId(String vendorCode, String keycloakId);
    Optional<Product> findByIdAndKeycloakId(Long id, String keycloakId);
    List<Product> findAllByKeycloakId(String keycloakId);
}
