package kz.wonder.wonderuserrepository.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import kz.wonder.wonderuserrepository.security.keycloak.KeycloakBaseUser;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class SellerRegistrationRequest extends KeycloakBaseUser {
    @NotNull(message = "Phone number not found")
    @Size(min = 10, max = 20, message = "Phone number must be in range 10-20")
    private String phoneNumber;

    @NotNull(message = "Please provide seller name")
    private String sellerName;

    @NotNull(message = "Please provide seller id")
    private String sellerId;

    @NotNull(message = "Please provide kaspi token ")
    private String tokenKaspi;

    @JsonIgnore
    private String keycloakId;
}
