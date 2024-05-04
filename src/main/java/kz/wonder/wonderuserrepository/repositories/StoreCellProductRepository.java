package kz.wonder.wonderuserrepository.repositories;

import kz.wonder.wonderuserrepository.entities.StoreCellProduct;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StoreCellProductRepository extends JpaRepository<StoreCellProduct, Long> {
    Optional<StoreCellProduct> findBySupplyBoxProductId(Long id);
}
