package kz.wonder.wonderuserrepository.repositories;

import kz.wonder.wonderuserrepository.entities.KaspiCity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface KaspiCityRepository extends JpaRepository<KaspiCity, Long> {
    boolean existsByName(String name);

    boolean existsByCode(String code);

    Optional<KaspiCity> findByKaspiId(String code);

    Optional<KaspiCity> findByName(String name);

    Optional<KaspiCity> findByCode(String code);

    @Query("select kc from KaspiCity kc " +
            "LEFT JOIN KaspiStore ks ON ks.kaspiCity.id = kc.id " +
            "WHERE ks.id = :kaspiId")
    Optional<KaspiCity> findByKaspiId(Long kaspiId);
}
