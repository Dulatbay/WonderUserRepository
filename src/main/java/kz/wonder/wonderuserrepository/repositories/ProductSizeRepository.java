package kz.wonder.wonderuserrepository.repositories;

import kz.wonder.wonderuserrepository.entities.ProductSize;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductSizeRepository extends JpaRepository<ProductSize, Long> {
    Optional<ProductSize> findByOriginVendorCode(String originVendorCode);
}
