package kz.wonder.wonderuserrepository.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
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

    @Digits(integer = 10, fraction = 0, message = "Phone number must have 10 integers")
    private String phoneNumber;

    @NotNull(message = "Please provide a store id")
    private Long storeId;

    @NotNull(message = "Please provide keycloak id")
    @JsonIgnore
    private String keycloakId;
}
