package kz.wonder.wonderuserrepository.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class StoreResponse {
    private Long id;
    private String kaspiId;
    private String address;
    private String street;
    private String city;
    private List<AvailableWorkTime> availableWorkTimes;
    private boolean enabled;
    private Long userId;

    @Data
    @Builder
    public static class AvailableWorkTime {
        private Long id;
        private Integer dayOfWeek;
        private String openTime;
        private String closeTime;
    }
}
