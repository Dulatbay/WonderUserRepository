package kz.wonder.wonderuserrepository.repositories;

import kz.wonder.wonderuserrepository.entities.KaspiCity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface KaspiCityRepository extends JpaRepository<KaspiCity, Long> {
    boolean existsByName(String name);
    boolean existsByCode(String code);
    Optional<KaspiCity> findByName(String name);
    Optional<KaspiCity> findByCode(String code);
}
