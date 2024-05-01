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
    // todo: add validation
    @NotNull(message = "Phone number not found")
    @Size(min = 10, max = 11)
    private String phoneNumber;

    // in kaspi
    @NotNull(message = "Please provide seller name")
    private String sellerName;

    @NotNull(message = "Please provide seller id")
    private String sellerId;

    @NotNull(message = "Please provide kaspi token ")
    private String tokenKaspi;

    @NotNull(message = "Please provide keycloak id")
    @JsonIgnore
    private String keycloakId;
}
