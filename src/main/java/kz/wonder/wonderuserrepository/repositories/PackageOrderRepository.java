package kz.wonder.wonderuserrepository.repositories;

import kz.wonder.wonderuserrepository.entities.PackageOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PackageOrderRepository extends JpaRepository<PackageOrder, Integer> {
}
