package kz.wonder.wonderuserrepository.repositories;

import kz.wonder.wonderuserrepository.entities.KaspiProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface KaspiProductCategoryRepository extends JpaRepository<KaspiProductCategory, Long> {
    Optional<KaspiProductCategory> findByCode(String code);
}
