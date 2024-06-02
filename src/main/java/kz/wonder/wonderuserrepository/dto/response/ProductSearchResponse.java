package kz.wonder.wonderuserrepository.dto.response;

import lombok.Data;

@Data
public class ProductSearchResponse {
    private Long productId;
    private String vendorCode;
    private String article;
    private String productName;
    private String shopName;
    private String cellCode;
    private Double price;
}
