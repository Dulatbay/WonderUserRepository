package kz.wonder.wonderuserrepository.dto.request;

import lombok.Data;

@Data
public class SellerUserUpdateRequest {
    private Long id;
    private String phoneNumber;

    private String sellerName;
    private String sellerId;
    private String tokenKaspi;

    private String firstName;
    private String lastName;
    private String email;

}
