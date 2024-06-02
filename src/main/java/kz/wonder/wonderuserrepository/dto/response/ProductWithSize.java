package kz.wonder.wonderuserrepository.dto.response;

import lombok.Data;

@Data
public class ProductWithSize {
    private String productName;
    private String productArticle;
    private Double width;
    private Double length;
    private Double weight;
    private Double height;
    private String comment;
}
