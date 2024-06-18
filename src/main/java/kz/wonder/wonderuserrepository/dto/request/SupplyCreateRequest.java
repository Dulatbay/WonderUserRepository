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
    @NotNull(message = "{requests.supply-create-request.please-provide-store-id}")
    @Positive(message = "{requests.supply-create-request.store-id-must-be-positive}")
    private Long storeId;

    @Convert(converter = LocalDateTimeAttributeConverter.class)
    private LocalDateTime selectedTime;

    @NotEmpty(message = "{requests.supply-create-request.please-provide-selected-boxes}")
    private List<SelectedBox> selectedBoxes;

    @Data
    public static class SelectedBox {
        @NotNull(message = "{requests.supply-create-request.please-provide-selected-box-id}")
        private Long selectedBoxId;
        @NotEmpty(message = "{requests.supply-create-request.please-provide-product-quantities}")
        private List<ProductQuantity> productQuantities;
    }

    @Data
    public static class ProductQuantity {
        @NotNull(message = "{requests.supply-create-request.please-provide-product-id}")
        @Positive(message = "{requests.supply-create-request.product-id-must-be-positive}")
        private Long productId;
        @NotNull(message = "{requests.supply-create-request.please-provide-quantity}")
        private Integer quantity;
    }
}
