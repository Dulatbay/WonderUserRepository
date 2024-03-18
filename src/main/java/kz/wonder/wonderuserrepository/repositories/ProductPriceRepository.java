package kz.wonder.wonderuserrepository.repositories;

import kz.wonder.wonderuserrepository.entities.Product;
import kz.wonder.wonderuserrepository.entities.ProductPrice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductPriceRepository extends JpaRepository<ProductPrice, Long> {
    Optional<ProductPrice> findByProductAndKaspiCityName(Product product, String cityName);

}
