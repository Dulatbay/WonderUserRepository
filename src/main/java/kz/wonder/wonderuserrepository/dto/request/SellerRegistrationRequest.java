package kz.wonder.wonderuserrepository.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SellerRegistrationRequest {
    // todo: add validation
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String password;

    // in kaspi
    private String sellerName;
    private String sellerId;
    private String tokenKaspi;

    @JsonIgnore
    private String keycloakId;
}
