package kz.wonder.wonderuserrepository.services;

import kz.wonder.wonderuserrepository.dto.request.EmployeeCreateRequest;
import kz.wonder.wonderuserrepository.dto.response.EmployeeResponse;
import kz.wonder.wonderuserrepository.entities.StoreEmployee;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.UserRepresentation;

import java.util.List;

public interface StoreEmployeeService {
    void createStoreEmployee(EmployeeCreateRequest employeeCreateRequest);

    EmployeeResponse getStoreEmployeeById(StoreEmployee storeEmployee, UserResource userResource);

    StoreEmployee getStoreEmployeeById(Long id);

    List<EmployeeResponse> getAllStoreEmployees(List<UserRepresentation> employeesInKeycloak);

    List<EmployeeResponse> getAllStoreEmployees(Long storeId, List<UserRepresentation> userRepresentations);

    StoreEmployee updateStoreEmployee(Long employeeId, Long storeId);

    StoreEmployee updateStoreEmployee(Long employeeId, Long storeId, String phoneNumber);

    void deleteStoreEmployee(StoreEmployee id);
}