package kz.wonder.wonderuserrepository.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

    @Min(value = -90, message = "latitude must be in range -90 and 90")
    @Max(value = 90, message = "latitude must be in range -90 and 90")
    private Long latitude;

    @Min(value = -180, message = "longitude must be in range -180 and 180")
    @Max(value = 180, message = "longitude must be in range -180 and 180")
    private Long longitude;

    @NotNull(message = "Day of week must be not null")
    @Size(min = 1, max = 7, message = "Номер недели должен находиться в диапазоне от 1 до 7.")
    private List<
            @Min(value = 1, message = "Номер недели должен находиться в диапазоне от 1 до 7.")
            @Max(value = 7, message = "Номер недели должен находиться в диапазоне от 1 до 7.") DayOfWeekWork> dayOfWeekWorks;
}
