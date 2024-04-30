package kz.wonder.wonderuserrepository.dto.request;


import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import kz.wonder.wonderuserrepository.security.keycloak.KeycloakBaseUser;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class EmployeeUpdateRequest extends KeycloakBaseUser {
    @Size(min = 10, max = 11, message = "Phone number must have 10 integers")
    private String phoneNumber;

    @NotNull(message = "Please provide store id")
    private Long storeId;
}
