package kz.wonder.wonderuserrepository.repositories;

import kz.wonder.wonderuserrepository.entities.ProductPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductPriceRepository extends JpaRepository<ProductPrice, Long> {
    Optional<ProductPrice> findByProductIdAndKaspiCityName(Long productId, String cityName);

    Optional<ProductPrice> findFirstByProductIdOrderByPriceDesc(Long productId);

    @Query("SELECT pp FROM ProductPrice pp " +
            "LEFT JOIN FETCH pp.product p " +
            "LEFT JOIN FETCH pp.kaspiCity ppkc " +
            "LEFT JOIN FETCH ppkc.kaspiStores " +
            "WHERE p.id IN :productIds")
    List<ProductPrice> findPricesByProductIds(@Param("productIds") List<Long> productIds);


}
