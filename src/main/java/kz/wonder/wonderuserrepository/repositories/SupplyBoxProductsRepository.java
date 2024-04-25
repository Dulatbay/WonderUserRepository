package kz.wonder.wonderuserrepository.repositories;

import kz.wonder.wonderuserrepository.entities.SupplyBoxProduct;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SupplyBoxProductsRepository extends JpaRepository<SupplyBoxProduct, Long> {

    List<SupplyBoxProduct> findAllByProductIdAndSupplyBoxSupplyId(Long productId, Long supplyBoxId);

    Optional<SupplyBoxProduct> findByArticle(String productArticle);

}
