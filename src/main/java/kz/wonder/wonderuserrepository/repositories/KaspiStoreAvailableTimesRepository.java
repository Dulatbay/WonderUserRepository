package kz.wonder.wonderuserrepository.repositories;

import kz.wonder.wonderuserrepository.entities.KaspiStore;
import kz.wonder.wonderuserrepository.entities.KaspiStoreAvailableTimes;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface KaspiStoreAvailableTimesRepository extends JpaRepository<KaspiStoreAvailableTimes, Long> {
    List<KaspiStoreAvailableTimes> findByKaspiStoreId(Long id);
    void deleteAllByKaspiStoreId(Long id);
}
