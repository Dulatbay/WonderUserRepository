package kz.wonder.wonderuserrepository.dto.request;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;


@Data
public class KaspiStoreChangeRequest {
    @NotNull(message = "{requests.kaspi-store-change-request.please-provide-kaspi-id}")
    private String kaspiId;

    @NotNull(message = "{requests.kaspi-store-change-request.enabled-cannot-be-null}")
    private boolean enabled;

    @NotNull(message = "{requests.kaspi-store-change-request.please-provide-city-id}")
    private Long cityId;

    @Nullable
    //@Size(min = 1, max = 50, message = "Street name must be in range 1-50")
    private String streetName;

    @Nullable
    //@Size(min = 1, max = 50, message = "Street number must be in range 1-50")
    private String streetNumber;

    @NotNull(message = "{requests.kaspi-store-change-request.day-of-week-must-be-not-null}")
    private List<DayOfWeekWork> dayOfWeekWorks;
}
