package kz.wonder.wonderuserrepository.dto.params;

import java.time.Duration;

public enum DurationParams {
    DAY, WEEK, MONTH, YEAR;

    public Duration getDuration() {
        return switch (this) {
            case DAY -> Duration.ofDays(1);
            case WEEK -> Duration.ofDays(7);
            case MONTH -> Duration.ofDays(30);
            case YEAR -> Duration.ofDays(365);
        };
    }
}
