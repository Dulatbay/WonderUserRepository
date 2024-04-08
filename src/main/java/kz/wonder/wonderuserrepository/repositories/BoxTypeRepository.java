package kz.wonder.wonderuserrepository.repositories;

import kz.wonder.wonderuserrepository.entities.BoxType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface BoxTypeRepository extends JpaRepository<BoxType, Long> {


	@Query(nativeQuery = true, value = """
    SELECT bt.*
    FROM schema_wonder.box_type as bt
    JOIN schema_wonder.kaspi_store_available_box_types as ksabt ON ksabt.box_type = bt.id
    WHERE ksabt.kaspi_store = :storeId AND bt.id = :boxTypeId
""")
	Optional<BoxType> findByIdInStore(@Param("boxTypeId") Long boxTypeId, @Param("storeId") Long storeId);}
