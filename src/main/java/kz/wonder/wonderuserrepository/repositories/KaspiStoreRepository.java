package kz.wonder.wonderuserrepository.repositories;

import kz.wonder.wonderuserrepository.entities.KaspiStore;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface KaspiStoreRepository extends JpaRepository<KaspiStore, Long> {
    List<KaspiStore> findAllByUserId(Long id);
}
