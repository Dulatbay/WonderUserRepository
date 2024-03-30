package kz.wonder.wonderuserrepository.controllers;

import kz.wonder.wonderuserrepository.constants.Utils;
import kz.wonder.wonderuserrepository.dto.request.EmployeeCreateRequest;
import kz.wonder.wonderuserrepository.dto.request.EmployeeUpdateRequest;
import kz.wonder.wonderuserrepository.dto.request.StoreEmployeeUpdatePassword;
import kz.wonder.wonderuserrepository.dto.response.EmployeeCreateResponse;
import kz.wonder.wonderuserrepository.dto.response.EmployeeResponse;
import kz.wonder.wonderuserrepository.security.keycloak.KeycloakRole;
import kz.wonder.wonderuserrepository.services.KeycloakService;
import kz.wonder.wonderuserrepository.services.StoreEmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
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
		var token = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();

		var authorities = Utils.getAuthorities(token.getAuthorities());

		List<EmployeeResponse> result = new ArrayList<>();
		var usersInKeycloak = keycloakService.getAllUsersByRole(KeycloakRole.STORE_EMPLOYEE);
		if (storeId != null)
			result = storeEmployeeService.getAllStoreEmployees(storeId, usersInKeycloak);
		else if (authorities.contains(KeycloakRole.SUPER_ADMIN.name()))
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

	@PutMapping("/update-password/{userId}")
	public ResponseEntity<Void> updatePassword(@PathVariable Long userId, @RequestBody StoreEmployeeUpdatePassword updatePassword) {
		var storeEmployee = storeEmployeeService.getStoreEmployeeById(userId);
		var keycloakUser = keycloakService.getUserById(storeEmployee.getWonderUser().getKeycloakId()).toRepresentation();

		updatePassword.setEmail(keycloakUser.getEmail());
		keycloakService.updatePassword(keycloakUser.getId(), updatePassword);
		return ResponseEntity.ok().build();
	}

	@PatchMapping("/{userId}")
	public ResponseEntity<EmployeeResponse> updateEmployee(@PathVariable Long userId, EmployeeUpdateRequest employeeUpdateRequest) {

		var userResource = keycloakService.updateUser(employeeUpdateRequest).toRepresentation();
		var employee = storeEmployeeService.updateStoreEmployee(userId, employeeUpdateRequest.getStoreId(), employeeUpdateRequest.getPhoneNumber());

		var updatedEmployee = EmployeeResponse.builder()
				.id(employee.getId())
				.email(userResource.getEmail())
				.firstName(userResource.getFirstName())
				.lastName(userResource.getLastName())
				.phoneNumber(employee.getWonderUser().getPhoneNumber())
				.storeId(employee.getKaspiStore().getId())
				.build();

		return ResponseEntity.ok(updatedEmployee);
	}
}
