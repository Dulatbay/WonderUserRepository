package kz.wonder.wonderuserrepository.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import kz.wonder.wonderuserrepository.constants.Utils;
import kz.wonder.wonderuserrepository.dto.request.EmployeeCreateRequest;
import kz.wonder.wonderuserrepository.dto.request.EmployeeUpdateRequest;
import kz.wonder.wonderuserrepository.dto.request.UpdatePasswordRequest;
import kz.wonder.wonderuserrepository.dto.response.EmployeeCreateResponse;
import kz.wonder.wonderuserrepository.dto.response.EmployeeResponse;
import kz.wonder.wonderuserrepository.security.authorizations.AccessForAdmins;
import kz.wonder.wonderuserrepository.security.authorizations.AccessForAdminsAndEmployee;
import kz.wonder.wonderuserrepository.security.keycloak.KeycloakBaseUser;
import kz.wonder.wonderuserrepository.security.keycloak.KeycloakRole;
import kz.wonder.wonderuserrepository.services.KeycloakService;
import kz.wonder.wonderuserrepository.services.StoreEmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;

@RequiredArgsConstructor
@RestController
@RequestMapping("/employees")
public class EmployeeController {
    private final StoreEmployeeService storeEmployeeService;
    private final KeycloakService keycloakService;
    private final MessageSource messageSource;

    @PostMapping
    @Operation(summary = "Create employee", description = "This endpoint allows the creation of a new employee. The request must include employee details such as email, password, first name and last name. If successful, it returns the created employee's email and password.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Successfully created employee")
    })
    @AccessForAdmins
    public ResponseEntity<EmployeeCreateResponse> createEmployee(@RequestBody @Valid EmployeeCreateRequest employeeCreateRequest) {
        var token = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var keycloakIdOfCreator = Utils.extractIdFromToken(token);
        var isSuperAdmin = Utils.getAuthorities(token.getAuthorities()).contains(KeycloakRole.SUPER_ADMIN.name());

        var userRepresentation = keycloakService.createUserByRole(employeeCreateRequest, KeycloakRole.STORE_EMPLOYEE);
        employeeCreateRequest.setKeycloakId(userRepresentation.getId());

        try {
            storeEmployeeService.createStoreEmployee(employeeCreateRequest, keycloakIdOfCreator, isSuperAdmin);
        } catch (Exception e) {
            keycloakService.deleteUserById(userRepresentation.getId());
            throw e;
        }
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new EmployeeCreateResponse(employeeCreateRequest.getEmail(), employeeCreateRequest.getPassword()));
    }

    @GetMapping
    @Operation(summary = "Get all employees", description = "Retrieve a list of employees based on the store ID. If a store ID is provided, only employees of that store are returned. If no store ID is provided, and the requester is a Super Admin, all employees are returned.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved a list of employees")
    })
    @AccessForAdmins
    public ResponseEntity<List<EmployeeResponse>> getEmployees(@RequestParam(value = "store-id", required = false)
                                                               Long storeId,
                                                               Locale locale) {
        var token = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var keycloakIdOfCreator = Utils.extractIdFromToken(token);
        var authorities = Utils.getAuthorities(token.getAuthorities());
        var isSuperAdmin = authorities.contains(KeycloakRole.SUPER_ADMIN.name());

        List<EmployeeResponse> result;
        var usersInKeycloak = keycloakService.getAllUsersByRole(KeycloakRole.STORE_EMPLOYEE);
        if (storeId != null)
            result = storeEmployeeService.getAllStoreEmployees(storeId, usersInKeycloak, isSuperAdmin, keycloakIdOfCreator);
        else if (isSuperAdmin)
            result = storeEmployeeService.getAllStoreEmployees(usersInKeycloak);
        else{
            String invalidStoreId = messageSource.getMessage("controllers.employee-controller.invalid-store-id", null, locale);
            throw new IllegalArgumentException(invalidStoreId);
        }
        return ResponseEntity.ok(result);
    }


    @GetMapping("/{userId}")
    @Operation(summary = "Get employee by ID", description = "Fetch details of a specific employee using their ID. The response includes the employee's personal and professional details. This operation ensures the requester has the necessary permissions to view the employee's information.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the employee")
    })
    @AccessForAdmins
    public ResponseEntity<EmployeeResponse> getEmployeeById(@PathVariable Long userId, Locale locale) {
        var token = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var keycloakIdOfCreator = Utils.extractIdFromToken(token);
        var isSuperAdmin = Utils.getAuthorities(token.getAuthorities()).contains(KeycloakRole.SUPER_ADMIN.name());

        var storeEmployee = storeEmployeeService.getStoreEmployeeById(userId);
        var isHisEmployee = storeEmployee.getKaspiStore().getWonderUser().getKeycloakId().equals(keycloakIdOfCreator);

        if (!isHisEmployee && !isSuperAdmin)
            throw new IllegalArgumentException(messageSource.getMessage("controllers.employee-controller.employee-doesn't-exist", null, locale));

        var userResource = keycloakService.getUserById(storeEmployee.getWonderUser().getKeycloakId());

        EmployeeResponse employeeResponse = storeEmployeeService.getStoreEmployeeById(storeEmployee, userResource, keycloakIdOfCreator);

        return ResponseEntity.ok(employeeResponse);
    }


    @DeleteMapping("/{userId}")
    @Operation(summary = "Delete employee by ID", description = "Delete an employee based on the ID. This endpoint checks if the requester has the right to delete the employee. If the employee exists and the requester is authorized, the employee is deleted from the system.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully deleted the employee")
    })
    @AccessForAdmins
    public ResponseEntity<Void> deleteEmployeeId(@PathVariable Long userId, Locale locale) {
        var token = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var keycloakIdOfCreator = Utils.extractIdFromToken(token);
        var isSuperAdmin = Utils.getAuthorities(token.getAuthorities()).contains(KeycloakRole.SUPER_ADMIN.name());

        var storeEmployee = storeEmployeeService.getStoreEmployeeById(userId);
        var isHisEmployee = storeEmployee.getKaspiStore().getWonderUser().getKeycloakId().equals(keycloakIdOfCreator);

        if (!isHisEmployee && !isSuperAdmin)
            throw new IllegalArgumentException(messageSource.getMessage("controllers.employee-controller.employee-doesn't-exist", null, locale));

        keycloakService.deleteUserById(storeEmployee.getWonderUser().getKeycloakId());
        storeEmployeeService.deleteStoreEmployee(storeEmployee);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/update-password/{userId}")
    @Operation(summary = "Update employee's password", description = "This endpoint updates employee's password")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully updated the password")
    })
    @AccessForAdminsAndEmployee
    public ResponseEntity<Void> updatePassword(@PathVariable Long userId, @RequestBody @Valid UpdatePasswordRequest updatePassword, Locale locale) {
        var token = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var keycloakIdOfCreator = Utils.extractIdFromToken(token);
        var isSuperAdmin = Utils.getAuthorities(token.getAuthorities()).contains(KeycloakRole.SUPER_ADMIN.name());


        var storeEmployee = storeEmployeeService.getStoreEmployeeById(userId);
        var isEmployee = storeEmployee.getKaspiStore().getWonderUser().getKeycloakId().equals(keycloakIdOfCreator);

        if (!isEmployee && !isSuperAdmin)
            throw new IllegalArgumentException(messageSource.getMessage("controllers.employee-controller.employee-doesn't-exist", null, locale));


        var keycloakUser = keycloakService.getUserById(storeEmployee.getWonderUser().getKeycloakId()).toRepresentation();

        updatePassword.setEmail(keycloakUser.getEmail());
        keycloakService.updatePassword(keycloakUser.getId(), updatePassword);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{userId}")
    @Operation(summary = "Update employee", description = "Update the details of an existing employee. The request must include updated employee information such as email, first name, last name and phone number. The operation checks if the requester is authorized to make updates to the employee's details.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully updated the employee")
    })
    @AccessForAdminsAndEmployee
    public ResponseEntity<EmployeeResponse> updateEmployee(@PathVariable Long userId, @RequestBody EmployeeUpdateRequest employeeUpdateRequest, Locale locale) {
        var token = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var keycloakIdOfCreator = Utils.extractIdFromToken(token);
        var isSuperAdmin = Utils.getAuthorities(token.getAuthorities()).contains(KeycloakRole.SUPER_ADMIN.name());

        var storeEmployee = storeEmployeeService.getStoreEmployeeById(userId);
        var isEmployee = storeEmployee.getKaspiStore().getWonderUser().getKeycloakId().equals(keycloakIdOfCreator);

        if (!isEmployee && !isSuperAdmin)
            throw new IllegalArgumentException(messageSource.getMessage("controllers.employee-controller.employee-doesn't-exist", null, locale));

        var keycloakBaseUser = new KeycloakBaseUser();
        keycloakBaseUser.setEmail(employeeUpdateRequest.getEmail());
        keycloakBaseUser.setFirstName(employeeUpdateRequest.getFirstName());
        keycloakBaseUser.setLastName(employeeUpdateRequest.getLastName());

        var userResource = keycloakService.updateUser(keycloakBaseUser).toRepresentation();
        var employee = storeEmployeeService.updateStoreEmployee(userId, employeeUpdateRequest.getStoreId(), employeeUpdateRequest.getPhoneNumber(), employeeUpdateRequest.getFirstName() + " " + employeeUpdateRequest.getLastName());

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
