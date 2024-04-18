package kz.wonder.wonderuserrepository.dto.request;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;


@Data
public class KaspiStoreChangeRequest {
    private String kaspiId;
    private boolean enabled;
    private Long cityId;
    private String streetName;
    private String streetNumber;
    private String town;
    private String district;
    private String building;
    private String apartment;
    private String formattedAddress;
    private Long latitude;
    private Long longitude;

    @NotNull(message = "Day of week must be not null")
    @Size(min = 1, max = 7, message = "Number of days should be between 1 and 7")
    private List<DayOfWeekWork> dayOfWeekWorks;
}
