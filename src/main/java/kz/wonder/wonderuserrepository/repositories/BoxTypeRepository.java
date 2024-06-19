package kz.wonder.wonderuserrepository.repositories;

import kz.wonder.wonderuserrepository.entities.BoxType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BoxTypeRepository extends JpaRepository<BoxType, Long> {


    @Query(nativeQuery = true, value = """
                SELECT bt.*
                FROM schema_wonder.box_type as bt
                JOIN schema_wonder.kaspi_store_available_box_types as ksabt ON ksabt.box_type = bt.id
                WHERE ksabt.kaspi_store = :storeId AND bt.id = :boxTypeId AND bt.deleted = false
            """)
    Optional<BoxType> findByIdInStore(@Param("boxTypeId") Long boxTypeId, @Param("storeId") Long storeId);


    @Query(nativeQuery = true, value="select bt.* " +
            "from schema_wonder.box_type as bt " +
            "LEFT JOIN schema_wonder.kaspi_store_available_box_types sbt ON sbt.box_type = bt.id " +
            "WHERE sbt.kaspi_store = :storeId AND bt.id IN :boxTypeIds AND bt.deleted = false")
    List<BoxType> findByIdsInStore(@Param("boxTypeIds") List<Long> boxTypeId, @Param("storeId") Long storeId);

    Optional<BoxType> findByIdAndDeletedIsFalse(Long id);

    List<BoxType> findAllByDeletedIsFalse();
}
