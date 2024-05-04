package kz.wonder.wonderuserrepository.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import kz.wonder.wonderuserrepository.entities.WonderUser;
import lombok.Data;

import java.util.List;


@Data
public class KaspiStoreCreateRequest {
    @JsonIgnore
    private WonderUser wonderUser;

    @NotNull(message = "Please provide kaspi id")
    private String kaspiId;

    @NotNull(message = "Please provide city id")
    @Positive(message = "City id must be positive")
    private Long cityId;

    @Size(min = 2, max = 50, message = "Street name must be in range 2-50 characters")
    private String streetName;

    @Size(min = 1, max = 5, message = "Street number must be in range 1-5 characters")
    private String streetNumber;

    @Size(min = 2, max = 50, message = "Town must be in range 2-50 characters")
    private String town;

    @Size(min = 2, max = 50, message = "District must be in range 2-50 characters")
    private String district;

    @Size(min = 1, max = 5, message = "Building must be in range 1-5 characters")
    private String building;

    @Size(min = 1, max = 5, message = "Apartment name must be in range 1-5 characters")
    private String apartment;

    @Min(value = -90, message = "latitude must be in range -90 and 90")
    @Max(value = 90, message = "latitude must be in range -90 and 90")
    private Long latitude;

    @Min(value = -180, message = "longitude must be in range -180 and 180")
    @Max(value = 180, message = "longitude must be in range -180 and 180")
    private Long longitude;

    @NotNull(message = "Day of week must be not null")
    @Size(min = 1, max = 7, message = "Number of days should be between 1 and 7")
    private List<DayOfWeekWork> dayOfWeekWorks;
}
