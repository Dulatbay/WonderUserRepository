package kz.wonder.wonderuserrepository.dto.response;

import lombok.Data;

@Data
public class SellerUserResponse  {
    private Long id;
    private String phoneNumber;

    private String sellerName;
    private String sellerId;
    private String tokenKaspi;

    private String firstName;
    private String lastName;
    private String email;
}
