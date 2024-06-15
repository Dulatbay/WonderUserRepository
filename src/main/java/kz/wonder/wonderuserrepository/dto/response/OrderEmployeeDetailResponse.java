package kz.wonder.wonderuserrepository.dto.response;


import jakarta.persistence.Convert;
import kz.wonder.wonderuserrepository.config.converters.LocalDateTimeAttributeConverter;
import kz.wonder.wonderuserrepository.entities.DeliveryMode;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderEmployeeDetailResponse {
    private List<Product> products;

    private DeliveryMode deliveryMode;

    @Convert(converter = LocalDateTimeAttributeConverter.class)
    private LocalDateTime deliveryTime;

    @Data
    public static class Product {
        private String productName;
        private String productArticle;
        private String productVendorCode;
        private String pathToProductBarcode;
        private String pathToBoxBarcode;
        private String productCell;
    }
}
