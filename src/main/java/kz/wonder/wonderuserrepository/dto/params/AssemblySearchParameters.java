package kz.wonder.wonderuserrepository.dto.params;

import jakarta.persistence.Convert;
import kz.wonder.wonderuserrepository.config.converters.LocalDateTimeAttributeConverter;
import kz.wonder.wonderuserrepository.entities.enums.DeliveryMode;
import kz.wonder.wonderuserrepository.entities.enums.ProductStateInStore;
import lombok.Data;

import java.time.LocalDate;

@Data
public class AssemblySearchParameters {
    @Convert(converter = LocalDateTimeAttributeConverter.class)
    private LocalDate orderCreationStartDate;

    @Convert(converter = LocalDateTimeAttributeConverter.class)
    private LocalDate orderCreationEndDate;

    private int page;
    private int size;
    private DeliveryMode deliveryMode;
    private String sortBy;
    private ProductStateInStore productStateInStore;

    public AssemblySearchParameters(LocalDate orderCreationStartDate, LocalDate orderCreationEndDate, int page, int size, DeliveryMode deliveryMode, String sortBy, ProductStateInStore productStateInStore) {
        this.orderCreationStartDate = orderCreationStartDate;
        this.orderCreationEndDate = orderCreationEndDate;
        this.page = page;
        this.size = size;
        this.deliveryMode = deliveryMode;
        this.sortBy = sortBy;
        this.productStateInStore = productStateInStore;
    }
}
