package kz.wonder.wonderuserrepository.repositories;

import kz.wonder.wonderuserrepository.entities.Supply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SupplyRepository extends JpaRepository<Supply, Long> {
    @Query("SELECT s FROM Supply s " +
            "JOIN FETCH s.kaspiStore sk " +
            "LEFT JOIN WonderUser w ON w.id = s.author.id " +
            "WHERE s.createdAt BETWEEN :start AND :end " +
            "AND sk.wonderUser.keycloakId = :keycloakId " +
            "ORDER BY s.id DESC ")
    List<Supply> findAllAdminSupplies(LocalDateTime start, LocalDateTime end, String keycloakId);


    @Query("SELECT s FROM Supply s " +
            "JOIN FETCH s.kaspiStore " +
            "LEFT JOIN WonderUser w ON w.id = s.author.id " +
            "WHERE s.createdAt BETWEEN :start AND :end " +
            "AND w.keycloakId = :keycloakId " +
            "ORDER BY s.id DESC "
    )
    List<Supply> findAllSellerSupplies(LocalDateTime start, LocalDateTime end, String keycloakId);

    Optional<Supply> findByIdAndAuthorKeycloakId(Long id, String keycloakId);

    @Query(nativeQuery = true, value = "SELECT s.* FROM schema_wonder.supply s " +
            "JOIN schema_wonder.kaspi_store ks ON s.store_id = ks.id " +
            "JOIN schema_wonder.store_employee se ON ks.id = se.kaspi_store_id " +
            "WHERE se.wonder_user_id = :employeeId AND (s.selected_time BETWEEN :start AND :end)" +
            "ORDER BY s.selected_time ")
    List<Supply> findAllSuppliesOfStorage(@Param("employeeId")
                                          Long id,
                                          @Param("start")
                                          long start,
                                          @Param("end")
                                          long end);
}
