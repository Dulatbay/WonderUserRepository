package kz.wonder.wonderuserrepository.controllers;

import kz.wonder.wonderuserrepository.dto.request.EmployeeCreateRequest;
import kz.wonder.wonderuserrepository.dto.response.EmployeeCreateResponse;
import kz.wonder.wonderuserrepository.security.keycloak.KeycloakRole;
import kz.wonder.wonderuserrepository.services.KeycloakService;
import kz.wonder.wonderuserrepository.services.StoreEmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

}
