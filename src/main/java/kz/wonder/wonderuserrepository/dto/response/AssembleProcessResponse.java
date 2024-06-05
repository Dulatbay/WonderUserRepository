package kz.wonder.wonderuserrepository.dto.response;

import kz.wonder.wonderuserrepository.entities.AssembleState;
import kz.wonder.wonderuserrepository.entities.DeliveryMode;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class AssembleProcessResponse {
    private Long assembleId;
    private List<Product> productsToProcess;
    private List<ProcessedProduct> processedProducts;
    private DeliveryMode deliveryMode;
    private LocalDateTime deadline;
    private String sellerName;
    private String startedEmployeeName;
    private String orderCode;
    private AssembleState assembleState;

    @Data
    public static class Product {
        private Long id;
        private String article;
        private String name;
        private String cellCode;
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class ProcessedProduct extends Product {
        private String processedEmployeeName;
        private LocalDateTime processedDate;
        private String waybill;
    }
}
