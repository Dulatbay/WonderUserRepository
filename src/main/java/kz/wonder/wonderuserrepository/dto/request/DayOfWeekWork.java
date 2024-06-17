package kz.wonder.wonderuserrepository.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record DayOfWeekWork(
        @Min(value = 1, message = "{requests.day-of-week-work.the-week-number-must-be-in-the-range-from-1-to-7}")
        @Max(value = 7, message = "{requests.day-of-week-work.the-week-number-must-be-in-the-range-from-1-to-7}")
        Integer numericDayOfWeek,
        @Size(min = 5, max = 5, message = "{requests.day-of-week-work.open-time-must-have-5-symbols}")
        String openTime,

        @Size(min = 5, max = 5, message = "{requests.day-of-week-work.close-time-must-have-5-symbols}")
        String closeTime) {
}
