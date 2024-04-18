package kz.wonder.wonderuserrepository.repositories;

import kz.wonder.wonderuserrepository.entities.KaspiStore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface KaspiStoreRepository extends JpaRepository<KaspiStore, Long> {
    List<KaspiStore> findAllByWonderUserKeycloakId(String id);
    Optional<KaspiStore> findByWonderUserKeycloakIdAndId(String keycloakId, Long id);
    Optional<KaspiStore> findByWonderUserIdAndKaspiId(Long userId, String kaspiId);

    @Query("SELECT ks FROM KaspiStore ks WHERE ks.apartment = :apartment and ks.streetName = :streetName and ks.streetNumber = :streetNumber and ks.town = :town and ks.building = :building and ks.district = :district")
    Optional<KaspiStore> findByStoreAddress(
            @Param("apartment") String apartment,
            @Param("streetName") String streetName,
            @Param("streetNumber") String streetNumber,
            @Param("town") String town,
            @Param("building") String building,
            @Param("district") String district);
}
