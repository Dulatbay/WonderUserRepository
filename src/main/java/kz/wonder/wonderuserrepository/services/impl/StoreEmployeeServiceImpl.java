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
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StoreEmployeeServiceImpl implements StoreEmployeeService {
	private final StoreEmployeeRepository storeEmployeeRepository;
	private final KaspiStoreRepository kaspiStoreRepository;
	private final UserRepository userRepository;

	@Override
	@Transactional
	public void createStoreEmployee(EmployeeCreateRequest employeeCreateRequest) {
		log.info("Employee Initialize started");
		var isPhoneNumberUsed = storeEmployeeRepository.existsByWonderUserPhoneNumber(employeeCreateRequest.getPhoneNumber());

		if (isPhoneNumberUsed) {
			throw new IllegalArgumentException("Phone already used");
		}

		WonderUser wonderUser = new WonderUser();
		wonderUser.setPhoneNumber(employeeCreateRequest.getPhoneNumber());
		wonderUser.setKeycloakId(employeeCreateRequest.getKeycloakId());

		final var store = kaspiStoreRepository.findById(employeeCreateRequest.getStoreId())
				.orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.BAD_REQUEST, "Store doesn't exist", "Please write correct ID of store"));


		// todo: проверка на то, что склад его

		StoreEmployee storeEmployee = new StoreEmployee();
		storeEmployee.setKaspiStore(store);
		storeEmployee.setWonderUser(wonderUser);
		userRepository.save(wonderUser);
		storeEmployeeRepository.save(storeEmployee);
		log.info("Employee successfully created. EmployeeID: {} KaspiStoreID: {}", storeEmployee.getId(), storeEmployee.getKaspiStore().getId());
	}

	@Override
	public EmployeeResponse getStoreEmployeeById(StoreEmployee storeEmployee, UserResource userResource) {
		final var keycloakUser = userResource.toRepresentation();

		// todo: проверка на то, что склад его

		return this.buildEmployeeResponse(keycloakUser, storeEmployee);
	}

	@Override
	public StoreEmployee getStoreEmployeeById(Long id) {
		return storeEmployeeRepository.findById(id)
				.orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND, "Employee doesn't exist", "Please try one more time with another params"));
	}

	@Override
	public List<EmployeeResponse> getAllStoreEmployees(List<UserRepresentation> employeesInKeycloak) {
		final var storeEmployees = storeEmployeeRepository.findAll();
		log.info("Getting all store employees. Size: {}", employeesInKeycloak.size());
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
				.id(storeEmployee.getId())
				.email(keycloakUser.getEmail())
				.firstName(keycloakUser.getFirstName())
				.lastName(keycloakUser.getLastName())
				.storeId(storeEmployee.getKaspiStore().getId())
				.phoneNumber(wonderUser.getPhoneNumber())
				.build();
	}

	@Override
	public List<EmployeeResponse> getAllStoreEmployees(Long storeId, List<UserRepresentation> userRepresentations) {
		// todo: check that it's his own store

		final var storeEmployees = storeEmployeeRepository.findAllByKaspiStoreId(storeId);

		log.info("Getting all store employees with size: {}", storeEmployees.size());

		return storeEmployees.stream()
				.map(storeEmployee -> toEmployeeResponse(storeEmployee, userRepresentations))
				.filter(Objects::nonNull)
				.collect(Collectors.toList());
	}

	@Override
	public StoreEmployee updateStoreEmployee(Long employeeId, Long storeId) {
		final var storeEmployee = getStoreEmployeeWithStoreId(employeeId, storeId);

		return storeEmployeeRepository.save(storeEmployee);
	}

	@Override
	public StoreEmployee updateStoreEmployee(Long employeeId, Long storeId, String phoneNumber) {
		final var storeEmployee = getStoreEmployeeWithStoreId(employeeId, storeId);

		storeEmployee.getWonderUser().setPhoneNumber(phoneNumber);

		log.info("Store employee update with id: {}", storeEmployee.getId());

		return storeEmployeeRepository.save(storeEmployee);
	}

	private StoreEmployee getStoreEmployeeWithStoreId(Long employeeId, Long storeId) {
		final var storeEmployee = storeEmployeeRepository.findById(employeeId)
				.orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.BAD_REQUEST, "Employee doesn't exist", "Please try one more time with another params"));
		final var kaspiStore = kaspiStoreRepository.findById(storeId)
				.orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.BAD_REQUEST, "Store doesn't exist", "Please try one more time with another params"));

		storeEmployee.setKaspiStore(kaspiStore);
		log.info("Getting Employee with StoreID: {}. EmployeeID: {}", storeId, employeeId);
		return storeEmployee;
	}

	@Override
	public void deleteStoreEmployee(StoreEmployee storeEmployee) {
		log.info("Deleting Employee");
		storeEmployeeRepository.delete(storeEmployee);
	}
}
