package kz.wonder.wonderuserrepository.repositories;

import kz.wonder.wonderuserrepository.entities.OrderPackage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderPackageRepository extends JpaRepository<OrderPackage, Integer> {
}
