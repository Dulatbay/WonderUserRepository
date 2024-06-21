package kz.wonder.wonderuserrepository.repositories;

import kz.wonder.wonderuserrepository.entities.KaspiStoreAvailableBoxTypes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KaspiStoreAvailableBoxTypesRepository extends JpaRepository<KaspiStoreAvailableBoxTypes, Long> {
    @Query("SELECT avb FROM KaspiStoreAvailableBoxTypes avb " +
            "LEFT JOIN FETCH avb.kaspiStore " +
            "LEFT JOIN FETCH avb.boxType bt " +
            "LEFT JOIN FETCH bt.images " +
            "WHERE avb.kaspiStore.id = :storeId ")
    List<KaspiStoreAvailableBoxTypes> findByKaspiStoreId(Long storeId);
}
