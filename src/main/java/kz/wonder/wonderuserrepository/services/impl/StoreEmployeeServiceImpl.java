package kz.wonder.wonderuserrepository.services.impl;

import kz.wonder.wonderuserrepository.dto.request.EmployeeCreateRequest;
import kz.wonder.wonderuserrepository.entities.StoreEmployee;
import kz.wonder.wonderuserrepository.entities.WonderUser;
import kz.wonder.wonderuserrepository.exceptions.DbObjectNotFoundException;
import kz.wonder.wonderuserrepository.repositories.KaspiStoreRepository;
import kz.wonder.wonderuserrepository.repositories.StoreEmployeeRepository;
import kz.wonder.wonderuserrepository.services.StoreEmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StoreEmployeeServiceImpl implements StoreEmployeeService {
	private final StoreEmployeeRepository storeEmployeeRepository;
	private final KaspiStoreRepository kaspiStoreRepository;

	@Override
	public void createStoreEmployee(EmployeeCreateRequest employeeCreateRequest) {
		WonderUser wonderUser = new WonderUser();
		wonderUser.setPhoneNumber(employeeCreateRequest.getPhoneNumber());
		wonderUser.setKeycloakId(employeeCreateRequest.getKeycloakId());

		final var store = kaspiStoreRepository.findById(employeeCreateRequest.getStoreId())
				.orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.BAD_REQUEST, "Store doesn't exist", "Please write correct ID of store"));


		StoreEmployee storeEmployee = new StoreEmployee();
		storeEmployee.setKaspiStore(store);
		storeEmployee.setWonderUser(wonderUser);
		storeEmployeeRepository.save(storeEmployee);
	}
}
