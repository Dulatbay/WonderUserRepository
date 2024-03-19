package kz.wonder.wonderuserrepository.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;


@Data
public class KaspiStoreChangeRequest {
    private String kaspiId;
    private String name;
    private boolean enabled;
    private Long cityId;
    private String street;
    private String apartment;
    @NotNull(message = "Day of week must be not null")
    @Size(min = 1, max = 7, message = "Number of days should be between 1 and 7")
    private List<DayOfWeekWork> dayOfWeekWorks;


}
