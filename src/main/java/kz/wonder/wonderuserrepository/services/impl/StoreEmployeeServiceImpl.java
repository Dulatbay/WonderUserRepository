package kz.wonder.wonderuserrepository.services.impl;

import jakarta.transaction.Transactional;
import kz.wonder.wonderuserrepository.dto.request.EmployeeCreateRequest;
import kz.wonder.wonderuserrepository.dto.response.EmployeeResponse;
import kz.wonder.wonderuserrepository.entities.StoreEmployee;
import kz.wonder.wonderuserrepository.entities.WonderUser;
import kz.wonder.wonderuserrepository.exceptions.DbObjectNotFoundException;
import kz.wonder.wonderuserrepository.repositories.KaspiStoreRepository;
import kz.wonder.wonderuserrepository.repositories.StoreEmployeeRepository;
import kz.wonder.wonderuserrepository.repositories.UserRepository;
import kz.wonder.wonderuserrepository.services.StoreEmployeeService;
import lombok.RequiredArgsConstructor;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StoreEmployeeServiceImpl implements StoreEmployeeService {
	private final StoreEmployeeRepository storeEmployeeRepository;
	private final KaspiStoreRepository kaspiStoreRepository;
	private final UserRepository userRepository;

	@Override
	@Transactional
	public void createStoreEmployee(EmployeeCreateRequest employeeCreateRequest) {
		WonderUser wonderUser = new WonderUser();
		wonderUser.setPhoneNumber(employeeCreateRequest.getPhoneNumber());
		wonderUser.setKeycloakId(employeeCreateRequest.getKeycloakId());

		final var store = kaspiStoreRepository.findById(employeeCreateRequest.getStoreId())
				.orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.BAD_REQUEST, "Store doesn't exist", "Please write correct ID of store"));


		StoreEmployee storeEmployee = new StoreEmployee();
		storeEmployee.setKaspiStore(store);
		storeEmployee.setWonderUser(wonderUser);
		userRepository.save(wonderUser);
		storeEmployeeRepository.save(storeEmployee);
	}

	@Override
	public EmployeeResponse getStoreEmployeeById(StoreEmployee storeEmployee, UserResource userResource) {
		final var wonderUser = storeEmployee.getWonderUser();

		final var keycloakUser = userResource.toRepresentation();

		return EmployeeResponse.builder()
				.email(keycloakUser.getEmail())
				.firstName(keycloakUser.getFirstName())
				.lastName(keycloakUser.getLastName())
				.storeId(storeEmployee.getKaspiStore().getId())
				.phoneNumber(wonderUser.getPhoneNumber())
				.build();
	}

	@Override
	public StoreEmployee getStoreEmployeeById(Long id) {
		return storeEmployeeRepository.findById(id)
				.orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND, "Employee doesn't exist", "Please try one more time with another params"));
	}

	@Override
	public List<EmployeeResponse> getAllStoreEmployees(List<UserRepresentation> employeesInKeycloak) {
		final var storeEmployees = storeEmployeeRepository.findAll();
		return storeEmployees.stream()
				.map(storeEmployee -> toEmployeeResponse(storeEmployee, employeesInKeycloak))
				.filter(Objects::nonNull)
				.collect(Collectors.toList());
	}

	private EmployeeResponse toEmployeeResponse(StoreEmployee storeEmployee, List<UserRepresentation> employeesInKeycloak) {
		UserRepresentation keycloakUser = findKeycloakUser(employeesInKeycloak, storeEmployee.getWonderUser().getKeycloakId());
		return (keycloakUser != null) ? buildEmployeeResponse(keycloakUser, storeEmployee) : null;
	}

	private UserRepresentation findKeycloakUser(List<UserRepresentation> employeesInKeycloak, String keyCloakId) {
		return employeesInKeycloak.stream()
				.filter(user -> user.getId().equals(keyCloakId))
				.findFirst()
				.orElse(null);
	}

	private EmployeeResponse buildEmployeeResponse(UserRepresentation keycloakUser, StoreEmployee storeEmployee) {
		WonderUser wonderUser = storeEmployee.getWonderUser();
		return EmployeeResponse.builder()
				.email(keycloakUser.getEmail())
				.firstName(keycloakUser.getFirstName())
				.lastName(keycloakUser.getLastName())
				.storeId(storeEmployee.getKaspiStore().getId())
				.phoneNumber(wonderUser.getPhoneNumber())
				.build();
	}

	@Override
	public List<EmployeeResponse> getAllStoreEmployees(Long storeId, List<UserRepresentation> userRepresentations) {
		final var storeEmployees = storeEmployeeRepository.findAllByKaspiStoreId(storeId);
		return storeEmployees.stream()
				.map(storeEmployee -> toEmployeeResponse(storeEmployee, userRepresentations))
				.filter(Objects::nonNull)
				.collect(Collectors.toList());
	}

	@Override
	public void updateStoreEmployee(Long employeeId, Long storeId) {
		final var storeEmployee = storeEmployeeRepository.findById(employeeId)
				.orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.BAD_REQUEST, "Employee doesn't exist", "Please try one more time with another params"));

		final var kaspiStore = kaspiStoreRepository.findById(storeId)
				.orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.BAD_REQUEST, "Store doesn't exist", "Please try one more time with another params"));


		storeEmployee.setKaspiStore(kaspiStore);

		storeEmployeeRepository.save(storeEmployee);
	}

	@Override
	public void deleteStoreEmployee(StoreEmployee storeEmployee) {
		storeEmployeeRepository.delete(storeEmployee);
	}
}
