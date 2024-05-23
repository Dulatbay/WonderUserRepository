package kz.wonder.wonderuserrepository.dto.response;

import kz.wonder.wonderuserrepository.entities.DeliveryMode;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class AssembleProcessResponse {
    private List<Product> productsToProcess;
    private List<Product> processedProducts;
    private DeliveryMode deliveryMode;
    private LocalDateTime deadline;
    private String sellerName;
    private String startedEmployeeName;

    @Data
    public static class Product {
        private Long id;
        private String article;
        private String name;
        private String cellCode;
    }

}
