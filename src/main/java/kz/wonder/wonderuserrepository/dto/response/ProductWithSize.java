package kz.wonder.wonderuserrepository.dto.response;

import kz.wonder.wonderuserrepository.entities.ProductStateInStore;
import lombok.Data;

@Data
public class ProductWithSize {
    private String productName;
    private String productArticle;
    private String vendorCode;
    private Double width;
    private Double length;
    private Double weight;
    private Double height;
    private String comment;
    private ProductStateInStore state;
}
