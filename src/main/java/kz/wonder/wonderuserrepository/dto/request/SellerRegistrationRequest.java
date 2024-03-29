package kz.wonder.wonderuserrepository.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import kz.wonder.wonderuserrepository.entities.KeycloakBaseUser;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class SellerRegistrationRequest extends KeycloakBaseUser {
    // todo: add validation

    // in kaspi
    private String sellerName;
    private String sellerId;
    private String tokenKaspi;

    @JsonIgnore
    private String keycloakId;
}
