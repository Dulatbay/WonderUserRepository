package kz.wonder.wonderuserrepository.repositories;

import kz.wonder.wonderuserrepository.entities.SupplyBox;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SupplyBoxRepository extends JpaRepository<SupplyBox, Long> {
   Optional<SupplyBox> findByVendorCode(String vendorCode);
}
