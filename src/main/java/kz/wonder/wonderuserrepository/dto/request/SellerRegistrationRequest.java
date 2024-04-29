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
    @NotNull
    private String sellerName;
    @NotNull
    private String sellerId;
    @NotNull
    private String tokenKaspi;

    @JsonIgnore
    private String keycloakId;
}
