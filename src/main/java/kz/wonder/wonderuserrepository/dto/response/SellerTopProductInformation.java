package kz.wonder.wonderuserrepository.dto.response;

import lombok.Data;

@Data
public class SellerTopProductInformation {
    private Long productId;
    private String productName;
    private Double productPrice;
    private Long count;
}
