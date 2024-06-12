package kz.wonder.wonderuserrepository.repositories;

import kz.wonder.wonderuserrepository.entities.StoreCell;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StoreCellRepository extends JpaRepository<StoreCell, Long> {
    Optional<StoreCell> findByKaspiStoreIdAndCode(Long storeId, String code);

    Optional<StoreCell> findByIdAndDeletedIsFalse(Long storeId);
}
