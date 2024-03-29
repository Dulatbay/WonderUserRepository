package kz.wonder.wonderuserrepository.services;

import kz.wonder.wonderuserrepository.dto.request.EmployeeCreateRequest;

public interface StoreEmployeeService {
	void createStoreEmployee(EmployeeCreateRequest employeeCreateRequest);
}
