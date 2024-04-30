package kz.wonder.wonderuserrepository.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;


@Data
public class KaspiStoreChangeRequest {
    @NotNull(message = "Please provide kaspi id")
    private String kaspiId;

    @NotNull(message = "enabled cannot be null")
    private boolean enabled;

    @NotNull(message = "Please provide city id")
    private Long cityId;

    @Size(min = 1, max = 50, message = "Street name must be in range 1-50")
    private String streetName;

    @Size(min = 1, max = 5, message = "Street number must be in range 1-5")
    private String streetNumber;

    @Size(min = 1, max = 50, message = "Town must be in range 1-50")
    private String town;

    @Size(min = 1, max = 50, message = "District must be in range 1-50")
    private String district;

    @Size(min = 1, max = 5, message = "Building must be in range 1-5")
    private String building;

    @Size(min = 1, max = 5, message = "Apartment must be in range 1-5")
    private String apartment;

    @Min(value = -90, message = "latitude must be in range -90 and 90")
    @Max(value = 90, message = "latitude must be in range -90 and 90")
    private Long latitude;

    @Min(value = -180, message = "longitude must be in range -180 and 180")
    @Max(value = 180, message = "longitude must be in range -180 and 180")
    private Long longitude;

    @NotNull(message = "Day of week must be not null")
    private List<DayOfWeekWork> dayOfWeekWorks;
}
