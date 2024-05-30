package kz.wonder.wonderuserrepository.dto.request;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.*;
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

    @Nullable
    @Size(min = 1, max = 50, message = "Street name must be in range 1-50")
    private String streetName;

    @Nullable
    @Size(min = 1, max = 50, message = "Street number must be in range 1-50")
    private String streetNumber;

    @Nullable
    @Size(min = 1, max = 50, message = "Town name must be in range 1-50")
    private String town;

    @Nullable
    @Size(min = 1, max = 50, message = "District must be in range 1-50")
    private String district;

    @Nullable
    @Size(min = 1, max = 100, message = "Building must be in range 1-100")
    private String building;

    @Nullable
    @Size(min = 1, max = 100, message = "Apartment must be in range 1-100")
    private String apartment;

    @Nullable
    @Min(value = -90, message = "Latitude must be in range -90 and 90")
    @Max(value = 90, message = "Latitude must be in range -90 and 90")
    private Long latitude;

    @Nullable
    @Min(value = -180, message = "Longitude must be in range -180 and 180")
    @Max(value = 180, message = "Longitude must be in range -180 and 180")
    private Long longitude;

    @NotNull(message = "Day of week must be not null")
    private List<DayOfWeekWork> dayOfWeekWorks;
}
