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
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.ws.rs.NotFoundException;
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
    private final MessageSource messageSource;

    @Override
    @Transactional
    public void createStoreEmployee(EmployeeCreateRequest employeeCreateRequest, String keycloakIdOfCreator, boolean isSuperAdmin) {
        var isPhoneNumberUsed = storeEmployeeRepository.existsByWonderUserPhoneNumber(employeeCreateRequest.getPhoneNumber());

        if (isPhoneNumberUsed)
            throw new IllegalArgumentException(messageSource.getMessage("services-impl.store-employee-service-impl.phone-already-used", null, LocaleContextHolder.getLocale()));

        WonderUser wonderUser = new WonderUser();
        wonderUser.setPhoneNumber(employeeCreateRequest.getPhoneNumber());
        wonderUser.setKeycloakId(employeeCreateRequest.getKeycloakId());
        wonderUser.setUsername(employeeCreateRequest.getFirstName() + " " + employeeCreateRequest.getLastName());

        final var store = kaspiStoreRepository.findById(employeeCreateRequest.getStoreId())
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.BAD_REQUEST, messageSource.getMessage("services-impl.store-employee-service-impl.store-not-found", null, LocaleContextHolder.getLocale()), messageSource.getMessage("services-impl.store-employee-service-impl.please-provide-correct-store-id", null, LocaleContextHolder.getLocale())));
        var isHisStore = store.getWonderUser().getKeycloakId().equals(keycloakIdOfCreator);


        if (!isSuperAdmin) {
            if (!isHisStore)
                throw new IllegalArgumentException(messageSource.getMessage("services-impl.store-employee-service-impl.store-not-found", null, LocaleContextHolder.getLocale()));
            if (!store.isEnabled())
                throw new IllegalArgumentException(messageSource.getMessage("services-impl.store-employee-service-impl.store-disabled", null, LocaleContextHolder.getLocale()));
        }


        StoreEmployee storeEmployee = new StoreEmployee();
        storeEmployee.setKaspiStore(store);
        storeEmployee.setWonderUser(wonderUser);
        userRepository.save(wonderUser);
        storeEmployeeRepository.save(storeEmployee);
        log.info("Employee successfully created. EmployeeID: {} KaspiStoreID: {}", storeEmployee.getId(), storeEmployee.getKaspiStore().getId());

    }

    @Override
    public EmployeeResponse getStoreEmployeeById(StoreEmployee storeEmployee, UserResource userResource, String keycloakIdOfCreator) {
        try {
            final var keycloakUser = userResource.toRepresentation();
            return this.buildEmployeeResponse(keycloakUser, storeEmployee);
        } catch (NotFoundException e) {
            throw new DbObjectNotFoundException(HttpStatus.NOT_FOUND, messageSource.getMessage("services-impl.store-employee-service-impl.store-employee-not-found", null, LocaleContextHolder.getLocale()), messageSource.getMessage("services-impl.store-employee-service-impl.please-provide-correct-id", null, LocaleContextHolder.getLocale()));
        }
    }

    @Override
    public StoreEmployee getStoreEmployeeById(Long id) {
        return storeEmployeeRepository.findById(id)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND, messageSource.getMessage("services-impl.store-employee-service-impl.store-employee-not-found", null, LocaleContextHolder.getLocale()), messageSource.getMessage("services-impl.store-employee-service-impl.please-try-again-with-different-parameters", null, LocaleContextHolder.getLocale())));
    }

    @Override
    public List<EmployeeResponse> getAllStoreEmployees(List<UserRepresentation> employeesInKeycloak) {
        final var storeEmployees = storeEmployeeRepository.findAll();
        log.info("Getting all store employees. StoreEmployees size: {}, Employees In Keycloak size: {}", storeEmployees.size(), employeesInKeycloak.size());

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
    public List<EmployeeResponse> getAllStoreEmployees(Long storeId, List<UserRepresentation> userRepresentations, boolean isSuperAdmin, String keycloakIdOfCreator) {
        var kaspiStore = kaspiStoreRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("Магазин не существует"));

        var isHisStore = kaspiStore.getWonderUser().getKeycloakId().equals(keycloakIdOfCreator);

        if (!isHisStore && !isSuperAdmin)
            throw new IllegalArgumentException(messageSource.getMessage("services-impl.store-employee-service-impl.store-not-found", null, LocaleContextHolder.getLocale()));


        final var storeEmployees = storeEmployeeRepository.findAllByKaspiStoreId(storeId);

        log.info("Getting all store employees with size: {}", storeEmployees.size());

        return storeEmployees.stream()
                .map(storeEmployee -> toEmployeeResponse(storeEmployee, userRepresentations))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public StoreEmployee updateStoreEmployee(Long employeeId, Long storeId, String phoneNumber, String username) {
        final var storeEmployee = getStoreEmployeeWithStoreId(employeeId, storeId);

        var wonderUser = storeEmployee.getWonderUser();
        wonderUser.setPhoneNumber(phoneNumber);
        wonderUser.setUsername(username);
        userRepository.save(wonderUser);

        log.info("Store employee update with id: {}", storeEmployee.getId());

        return storeEmployeeRepository.save(storeEmployee);
    }

    @Override
    public StoreEmployee updateStoreEmployee(Long employeeId, Long storeId, String phoneNumber) {
        final var storeEmployee = getStoreEmployeeWithStoreId(employeeId, storeId);

        storeEmployee.getWonderUser().setPhoneNumber(phoneNumber);

        return storeEmployeeRepository.save(storeEmployee);
    }

    private StoreEmployee getStoreEmployeeWithStoreId(Long employeeId, Long storeId) {
        final var storeEmployee = storeEmployeeRepository.findById(employeeId)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.BAD_REQUEST, messageSource.getMessage("services-impl.store-employee-service-impl.store-employee-not-found", null, LocaleContextHolder.getLocale()), messageSource.getMessage("services-impl.store-employee-service-impl.please-try-again-with-different-parameters", null, LocaleContextHolder.getLocale())));
        final var kaspiStore = kaspiStoreRepository.findById(storeId)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.BAD_REQUEST, messageSource.getMessage("services-impl.store-employee-service-impl.store-not-found", null, LocaleContextHolder.getLocale()), messageSource.getMessage("services-impl.store-employee-service-impl.please-try-again-with-different-parameters", null, LocaleContextHolder.getLocale())));

        log.info("Getting Employee with StoreID: {}. EmployeeID: {}", storeId, employeeId);

        storeEmployee.setKaspiStore(kaspiStore);
        return storeEmployee;
    }

    @Override
    public void deleteStoreEmployee(StoreEmployee storeEmployee) {
        log.info("Deleting Employee");
        storeEmployeeRepository.delete(storeEmployee);
    }
}
