package kz.wonder.wonderuserrepository.repositories;

import kz.wonder.wonderuserrepository.entities.KaspiStore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface KaspiStoreRepository extends JpaRepository<KaspiStore, Long> {
    List<KaspiStore> findAllByWonderUserKeycloakIdAndDeletedIsFalse(String id);

    Optional<KaspiStore> findByWonderUserKeycloakIdAndIdAndDeletedIsFalse(String keycloakId, Long id);

    Optional<KaspiStore> findByOriginAddressId(String originAddressId);

    Optional<KaspiStore> findByPickupPointId(String pickupPointId);

    List<KaspiStore> findAllByEnabledIsTrueAndDeletedIsFalse();

    Optional<KaspiStore> findByIdAndDeletedIsFalse(Long id);

    List<KaspiStore> findAllByDeletedIsFalse();

    @Query("SELECT ks FROM KaspiStore ks " +
            "WHERE (:streetName is null  or ks.streetName = :streetName) and " +
            "      (:streetNumber is null or ks.streetNumber = :streetNumber) and " +
            "       ks.deleted = false and" +
            "       ks.kaspiCity.id = :kaspiCityId")
    Optional<KaspiStore> findByStoreAddress(
            @Param("streetName") String streetName,
            @Param("streetNumber") String streetNumber,
            @Param("kaspiCityId") Long kaspiCityId);
}
