package kz.wonder.wonderuserrepository.repositories;

import kz.wonder.wonderuserrepository.entities.KaspiStore;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface KaspiStoreRepository extends JpaRepository<KaspiStore, Long> {
    List<KaspiStore> findAllByWonderUserKeycloakId(String id);

    Optional<KaspiStore> findByWonderUserKeycloakIdAndId(String keycloakId, Long id);

    Optional<KaspiStore> findByOriginAddressId(String originAddressId);

    Optional<KaspiStore> findByKaspiIdAndWonderUserKeycloakId(String kaspiId, String keycloakId);

    List<KaspiStore> findAllByEnabledIsTrue();

    @Query("SELECT ks FROM KaspiStore ks " +
            "WHERE (:apartment is null or ks.apartment = :apartment) and " +
            "      (:streetName is null  or ks.streetName = :streetName) and " +
            "      (:streetNumber is null or ks.streetNumber = :streetNumber) and " +
            "      (:town is null or ks.town = :town) and " +
            "      (:building is null or ks.building = :building) and  " +
            "      (:district is null or ks.district = :district)")
    Optional<KaspiStore> findByStoreAddress(
            @Param("apartment") String apartment,
            @Param("streetName") String streetName,
            @Param("streetNumber") String streetNumber,
            @Param("town") String town,
            @Param("building") String building,
            @Param("district") String district);
}
