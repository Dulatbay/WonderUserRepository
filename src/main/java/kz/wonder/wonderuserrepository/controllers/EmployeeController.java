package kz.wonder.wonderuserrepository.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import kz.wonder.wonderuserrepository.constants.Utils;
import kz.wonder.wonderuserrepository.dto.request.EmployeeCreateRequest;
import kz.wonder.wonderuserrepository.dto.request.EmployeeUpdateRequest;
import kz.wonder.wonderuserrepository.dto.request.UpdatePasswordRequest;
import kz.wonder.wonderuserrepository.dto.response.EmployeeCreateResponse;
import kz.wonder.wonderuserrepository.dto.response.EmployeeResponse;
import kz.wonder.wonderuserrepository.security.keycloak.KeycloakBaseUser;
import kz.wonder.wonderuserrepository.security.keycloak.KeycloakRole;
import kz.wonder.wonderuserrepository.services.KeycloakService;
import kz.wonder.wonderuserrepository.services.StoreEmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/employees")
public class EmployeeController {
    private final StoreEmployeeService storeEmployeeService;
    private final KeycloakService keycloakService;

    @PostMapping
    @Operation(summary = "Create employee", description = "This endpoint creates an employee")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Successfully created employee")
    })
    public ResponseEntity<EmployeeCreateResponse> createEmployee(@RequestBody EmployeeCreateRequest employeeCreateRequest) {
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
    @Operation(summary = "Get all employees", description = "Returns a list of employees based on store ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved a list of employees")
    })
    public ResponseEntity<List<EmployeeResponse>> getEmployees(@RequestParam(value = "store-id", required = false)
                                                               Long storeId) {
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
        else
            throw new IllegalArgumentException("Invalid store-id");
        return ResponseEntity.ok(result);
    }


    @GetMapping("/{userId}")
    @Operation(summary = "Get employee by ID", description = "This endpoint returns an employee by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the employee")
    })
    public ResponseEntity<EmployeeResponse> getEmployeeById(@PathVariable Long userId) {
        var token = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var keycloakIdOfCreator = Utils.extractIdFromToken(token);
        var isSuperAdmin = Utils.getAuthorities(token.getAuthorities()).contains(KeycloakRole.SUPER_ADMIN.name());

        var storeEmployee = storeEmployeeService.getStoreEmployeeById(userId);
        var isHisEmployee = storeEmployee.getKaspiStore().getWonderUser().getKeycloakId().equals(keycloakIdOfCreator);

        if (!isHisEmployee && !isSuperAdmin)
            throw new IllegalArgumentException("Employee doesn't exist");

        var userResource = keycloakService.getUserById(storeEmployee.getWonderUser().getKeycloakId());

        EmployeeResponse employeeResponse = storeEmployeeService.getStoreEmployeeById(storeEmployee, userResource, keycloakIdOfCreator);

        return ResponseEntity.ok(employeeResponse);
    }


    @DeleteMapping("/{userId}")
    @Operation(summary = "Delete employee by ID", description = "This endpoint deletes the employee based on ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully deleted the employee")
    })
    public ResponseEntity<Void> deleteEmployeeId(@PathVariable Long userId) {
        var token = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var keycloakIdOfCreator = Utils.extractIdFromToken(token);
        var isSuperAdmin = Utils.getAuthorities(token.getAuthorities()).contains(KeycloakRole.SUPER_ADMIN.name());

        var storeEmployee = storeEmployeeService.getStoreEmployeeById(userId);
        var isHisEmployee = storeEmployee.getKaspiStore().getWonderUser().getKeycloakId().equals(keycloakIdOfCreator);

        if (!isHisEmployee && !isSuperAdmin)
            throw new IllegalArgumentException("Employee doesn't exist");

        keycloakService.deleteUserById(storeEmployee.getWonderUser().getKeycloakId());
        storeEmployeeService.deleteStoreEmployee(storeEmployee);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/update-password/{userId}")
    @Operation(summary = "Update employee password", description = "This endpoint updates an employee's password")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully updated the password")
    })
    public ResponseEntity<Void> updatePassword(@PathVariable Long userId, @RequestBody UpdatePasswordRequest updatePassword) {
        var token = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var keycloakIdOfCreator = Utils.extractIdFromToken(token);
        var isSuperAdmin = Utils.getAuthorities(token.getAuthorities()).contains(KeycloakRole.SUPER_ADMIN.name());


        var storeEmployee = storeEmployeeService.getStoreEmployeeById(userId);
        var isHisEmployee = storeEmployee.getKaspiStore().getWonderUser().getKeycloakId().equals(keycloakIdOfCreator);

        if (!isHisEmployee && !isSuperAdmin)
            throw new IllegalArgumentException("Employee doesn't exist");


        var keycloakUser = keycloakService.getUserById(storeEmployee.getWonderUser().getKeycloakId()).toRepresentation();

        updatePassword.setEmail(keycloakUser.getEmail());
        keycloakService.updatePassword(keycloakUser.getId(), updatePassword);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{userId}")
    @Operation(summary = "Update employee", description = "This endpoint updates an employee")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully updated the employee")
    })
    public ResponseEntity<EmployeeResponse> updateEmployee(@PathVariable Long userId, @RequestBody EmployeeUpdateRequest employeeUpdateRequest) {
        var token = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var keycloakIdOfCreator = Utils.extractIdFromToken(token);
        var isSuperAdmin = Utils.getAuthorities(token.getAuthorities()).contains(KeycloakRole.SUPER_ADMIN.name());

        var storeEmployee = storeEmployeeService.getStoreEmployeeById(userId);
        var isHisEmployee = storeEmployee.getKaspiStore().getWonderUser().getKeycloakId().equals(keycloakIdOfCreator);

        if (!isHisEmployee && !isSuperAdmin)
            throw new IllegalArgumentException("Employee doesn't exist");

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
