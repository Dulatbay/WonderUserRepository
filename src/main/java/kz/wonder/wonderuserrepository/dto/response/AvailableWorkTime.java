package kz.wonder.wonderuserrepository.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AvailableWorkTime {
    private Long id;
    private Integer dayOfWeek;
    private String openTime;
    private String closeTime;
}