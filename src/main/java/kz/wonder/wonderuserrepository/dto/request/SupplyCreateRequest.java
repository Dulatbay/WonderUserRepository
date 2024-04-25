package kz.wonder.wonderuserrepository.dto.request;

import jakarta.persistence.Convert;
import kz.wonder.wonderuserrepository.config.converters.LocalDateTimeAttributeConverter;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class SupplyCreateRequest {
    private Long storeId;

    @Convert(converter = LocalDateTimeAttributeConverter.class)
    private LocalDateTime selectedTime;

    private List<SelectedBox> selectedBoxes;

    @Data
    public static class SelectedBox {
        private Long selectedBoxId;
        private List<ProductQuantity> productQuantities;
    }

    @Data
    public static class ProductQuantity {
        private Long productId;
        private Integer quantity;
    }
}
