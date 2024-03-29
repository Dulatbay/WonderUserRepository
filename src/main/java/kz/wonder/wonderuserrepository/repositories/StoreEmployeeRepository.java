package kz.wonder.wonderuserrepository.repositories;

import kz.wonder.wonderuserrepository.entities.StoreEmployee;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoreEmployeeRepository extends JpaRepository<StoreEmployee, Long> {

}
