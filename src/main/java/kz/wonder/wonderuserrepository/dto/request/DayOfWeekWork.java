package kz.wonder.wonderuserrepository.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record DayOfWeekWork(
        @Min(value = 1, message = "Week day must be in range 1-7")
        @Max(value = 7, message = "Week day must be in range 1-7")
        Integer numericDayOfWeek,
        @Size(min = 5, max = 5, message = "Open time must have 5 symbols")
        String openTime,

        @Size(min = 5, max = 5, message = "Close time must have 5 symbols")
        String closeTime) {
}
