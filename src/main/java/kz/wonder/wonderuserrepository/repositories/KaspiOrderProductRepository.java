package kz.wonder.wonderuserrepository.repositories;

import kz.wonder.wonderuserrepository.entities.KaspiOrderProduct;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface KaspiOrderProductRepository extends JpaRepository<KaspiOrderProduct, Long> {
    Optional<KaspiOrderProduct> findByProductIdAndOrderId(Long productId, Long orderId);
}
