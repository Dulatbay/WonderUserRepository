package kz.wonder.wonderuserrepository.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record DayOfWeekWork(
        @Min(value = 1, message = "Week day must be in range 1-7")
        @Max(value = 7, message = "Week day must be in range 1-7")
        Integer numericDayOfWeek,
        String openTime,
        String closeTime) {
}
