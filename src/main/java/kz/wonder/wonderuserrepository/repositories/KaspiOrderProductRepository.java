package kz.wonder.wonderuserrepository.repositories;

import kz.wonder.wonderuserrepository.entities.KaspiOrderProduct;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface KaspiOrderProductRepository extends JpaRepository<KaspiOrderProduct, Long> {
    boolean existsByKaspiId(String kaspiId);
}
