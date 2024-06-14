package kz.wonder.wonderuserrepository.repositories;

import kz.wonder.wonderuserrepository.entities.KaspiOrderProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface KaspiOrderProductRepository extends JpaRepository<KaspiOrderProduct, Long> {
    boolean existsByKaspiId(String kaspiId);

    @Query("select kop from KaspiOrderProduct kop WHERE kop.product.keycloakId = :keycloakId")
    List<KaspiOrderProduct> findTopSellerProducts(@Param("keycloakId") String keycloakId);
}
