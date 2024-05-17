package kz.wonder.wonderuserrepository.dto.request;

import jakarta.persistence.Convert;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import kz.wonder.wonderuserrepository.config.converters.LocalDateTimeAttributeConverter;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class SupplyCreateRequest {
    @NotNull(message = "Please provide store id")
    @Positive(message = "Store id must be positive")
    private Long storeId;

    @Convert(converter = LocalDateTimeAttributeConverter.class)
    private LocalDateTime selectedTime;

    @NotEmpty(message = "Please provide selected boxes")
    private List<SelectedBox> selectedBoxes;

    @Data
    public static class SelectedBox {
        @NotNull(message = "Please provide selected box id")
        private Long selectedBoxId;
        @NotEmpty(message = "Please provide product quantities")
        private List<ProductQuantity> productQuantities;
    }

    @Data
    public static class ProductQuantity {
        @NotNull(message = "Please provide product it")
        @Positive(message = "Product id must be positive")
        private Long productId;
        @NotNull(message = "Please provide quantity")
        private Integer quantity;
    }
}
