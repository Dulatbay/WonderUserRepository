package kz.wonder.wonderuserrepository.repositories;

import kz.wonder.wonderuserrepository.entities.KaspiStoreAvailableTimes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Set;

public interface KaspiStoreAvailableTimesRepository extends JpaRepository<KaspiStoreAvailableTimes, Long> {
    @Query("SELECT kt FROM KaspiStoreAvailableTimes kt " +
            "LEFT JOIN FETCH kt.kaspiStore " +
            "WHERE kt.kaspiStore.id = :storeId ")
    List<KaspiStoreAvailableTimes> findByKaspiStoreId(Long storeId);
}
