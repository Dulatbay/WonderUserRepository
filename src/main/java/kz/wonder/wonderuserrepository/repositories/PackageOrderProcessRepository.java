package kz.wonder.wonderuserrepository.repositories;

import kz.wonder.wonderuserrepository.entities.PackageOrderProcess;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PackageOrderProcessRepository extends JpaRepository<PackageOrderProcess, Integer> {
}
