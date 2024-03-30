package kz.wonder.wonderuserrepository.repositories;

import kz.wonder.wonderuserrepository.entities.StoreEmployee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StoreEmployeeRepository extends JpaRepository<StoreEmployee, Long> {
	List<StoreEmployee> findAllByKaspiStoreId(Long storeId);
	boolean existsByWonderUserPhoneNumber(String phoneNumber);
}
