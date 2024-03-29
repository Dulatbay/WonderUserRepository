package kz.wonder.wonderuserrepository.controllers;

import kz.wonder.wonderuserrepository.dto.request.EmployeeCreateRequest;
import kz.wonder.wonderuserrepository.dto.response.EmployeeCreateResponse;
import kz.wonder.wonderuserrepository.dto.response.EmployeeResponse;
import kz.wonder.wonderuserrepository.security.keycloak.KeycloakRole;
import kz.wonder.wonderuserrepository.services.KeycloakService;
import kz.wonder.wonderuserrepository.services.StoreEmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/employees")
public class EmployeeController {
	private final StoreEmployeeService storeEmployeeService;
	private final KeycloakService keycloakService;

	@PostMapping
	public ResponseEntity<EmployeeCreateResponse> createEmployee(@RequestBody EmployeeCreateRequest employeeCreateRequest) {
		var userRepresentation = keycloakService.createUserByRole(employeeCreateRequest, KeycloakRole.STORE_EMPLOYEE);
		employeeCreateRequest.setKeycloakId(userRepresentation.getId());

		try {
			storeEmployeeService.createStoreEmployee(employeeCreateRequest);
		} catch (Exception e) {
			keycloakService.deleteUserById(userRepresentation.getId());
			throw e;
		}
		return ResponseEntity
				.status(HttpStatus.CREATED)
				.body(new EmployeeCreateResponse(employeeCreateRequest.getEmail(), employeeCreateRequest.getPassword()));
	}

	@GetMapping
	public ResponseEntity<List<EmployeeResponse>> getEmployees(@RequestParam(value = "store-id", required = false)
	                                                           Long storeId) {
		List<EmployeeResponse> result;
		var usersInKeycloak = keycloakService.getAllUsersByRole(KeycloakRole.STORE_EMPLOYEE);
		if (storeId != null)
			result = storeEmployeeService.getAllStoreEmployees(storeId, usersInKeycloak);
		else
			result = storeEmployeeService.getAllStoreEmployees(usersInKeycloak);
		return ResponseEntity.ok(result);
	}


	@GetMapping("/{userId}")
	public ResponseEntity<EmployeeResponse> getEmployeeById(@PathVariable Long userId) {
		var storeEmployee = storeEmployeeService.getStoreEmployeeById(userId);

		var userResource = keycloakService.getUserById(storeEmployee.getWonderUser().getKeycloakId());

		EmployeeResponse employeeResponse = storeEmployeeService.getStoreEmployeeById(storeEmployee, userResource);

		return ResponseEntity.ok(employeeResponse);
	}


	@DeleteMapping("/{userId}")
	public ResponseEntity<Void> deleteEmployeeId(@PathVariable Long userId) {
		var storeEmployee = storeEmployeeService.getStoreEmployeeById(userId);
		keycloakService.deleteUserById(storeEmployee.getWonderUser().getKeycloakId());
		storeEmployeeService.deleteStoreEmployee(storeEmployee);
		return ResponseEntity.noContent().build();
	}

}
