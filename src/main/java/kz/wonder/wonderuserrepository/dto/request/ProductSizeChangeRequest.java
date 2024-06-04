package kz.wonder.wonderuserrepository.dto.request;

import lombok.Data;

@Data
public class ProductSizeChangeRequest {
    private Double weight;
    private Double height;
    private Double length;
    private Double width;
    private String comment;
}
