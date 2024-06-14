package kz.wonder.wonderuserrepository.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import kz.wonder.wonderuserrepository.security.keycloak.KeycloakBaseUser;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
public class EmployeeCreateRequest extends KeycloakBaseUser {
    @NotNull(message = "Please provide a first name")
    private String firstName;

    @NotNull(message = "Please provide a last name")
    private String lastName;

    @Email(message = "Please provide a valid email address")
    private String email;

    @Size(min = 10, max = 20, message = "Phone number must be in range 10-20")
    private String phoneNumber;

    @NotNull(message = "Please provide a store id")
    private Long storeId;

    @JsonIgnore
    private String keycloakId;
}
