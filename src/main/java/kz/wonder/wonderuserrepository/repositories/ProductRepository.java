package kz.wonder.wonderuserrepository.repositories;

import kz.wonder.wonderuserrepository.dto.response.ProductResponse;
import kz.wonder.wonderuserrepository.entities.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findByVendorCodeAndKeycloakId(String vendorCode, String keycloakId);

    Optional<Product> findByOriginalVendorCodeAndKeycloakId(String originalVendorCode, String keycloakId);

    Optional<Product> findByIdAndKeycloakId(Long id, String keycloakId);

    Page<Product> findAllBy(Pageable pageable);

    Page<Product> findAllByKeycloakId(String keycloakUserId, Pageable pageable);
    List<Product> findAllByKeycloakId(String keycloakUserId);
}
