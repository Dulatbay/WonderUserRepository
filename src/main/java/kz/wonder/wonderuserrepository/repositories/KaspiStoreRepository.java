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

    Optional<KaspiStore> findByOriginAddressId(String originAddressId);

    Optional<KaspiStore> findByPickupPointIdAndWonderUserKeycloakId(String pickupPointId, String keycloakId);

    List<KaspiStore> findAllByEnabledIsTrue();

    @Query("SELECT ks FROM KaspiStore ks " +
            "WHERE (:streetName is null  or ks.streetName = :streetName) and " +
            "      (:streetNumber is null or ks.streetNumber = :streetNumber)")
    Optional<KaspiStore> findByStoreAddress(
            @Param("streetName") String streetName,
            @Param("streetNumber") String streetNumber);
}
