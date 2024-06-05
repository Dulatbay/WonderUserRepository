package kz.wonder.wonderuserrepository.dto.response;

import lombok.Data;

@Data
public class ProductWithCount {
    private String article;
    private String name;
    private Long count;
    private Long storeId;
    private String storeFormattedAddress;
}
