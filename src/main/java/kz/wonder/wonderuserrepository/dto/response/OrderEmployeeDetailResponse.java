package kz.wonder.wonderuserrepository.dto.response;


import jakarta.persistence.Convert;
import kz.wonder.wonderuserrepository.config.converters.LocalDateTimeAttributeConverter;
import kz.wonder.wonderuserrepository.dto.base.OrderWithStatus;
import kz.wonder.wonderuserrepository.entities.enums.DeliveryMode;
import kz.wonder.wonderuserrepository.entities.enums.ProductStateInStore;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderEmployeeDetailResponse extends OrderWithStatus {
    private List<Product> products;

    private DeliveryMode deliveryMode;
    private String orderCode;
    private String waybill;

    @Convert(converter = LocalDateTimeAttributeConverter.class)
    private LocalDateTime deliveryTime;

    @Data
    public static class Product {
        private String productName;
        private String productArticle;
        private String productVendorCode;
        private String pathToProductBarcode;
        private String pathToBoxBarcode;
        private ProductStateInStore productStateInStore;
        private PackageInfo packageInfo;
        private String productCell;
    }

    @Data
    public static class PackageInfo {
        private LocalDateTime startedAt;
        private LocalDateTime finishedAt;
    }
}
