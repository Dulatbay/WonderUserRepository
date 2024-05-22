package kz.wonder.wonderuserrepository.dto.params;

import jakarta.persistence.Convert;
import kz.wonder.wonderuserrepository.config.converters.LocalDateTimeAttributeConverter;
import kz.wonder.wonderuserrepository.dto.enums.AssemblyMode;
import kz.wonder.wonderuserrepository.dto.enums.DeliveryMode;
import lombok.Data;

import javax.management.ConstructorParameters;
import java.time.LocalDate;

@Data
public class AssemblySearchParameters {
    @Convert(converter = LocalDateTimeAttributeConverter.class)
    private LocalDate startDate;

    @Convert(converter = LocalDateTimeAttributeConverter.class)
    private LocalDate endDate;

    private int page = 0;
    private int size = 10;
    private DeliveryMode deliveryMode;
    private AssemblyMode assemblyMode;
    private String sortBy;

    public AssemblySearchParameters(LocalDate startDate, LocalDate endDate, int page, int size, DeliveryMode deliveryMode, AssemblyMode assemblyMode, String sortBy) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.page = page;
        this.size = size;
        this.deliveryMode = deliveryMode;
        this.assemblyMode = assemblyMode;
        this.sortBy = sortBy;
    }
}
