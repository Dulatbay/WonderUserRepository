package kz.wonder.wonderuserrepository.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class StoreResponse {
    private Long id;
    private String kaspiId;
    private City city;
    private String streetName;
    private String streetNumber;
    private String formattedAddress;
    private List<AvailableWorkTime> availableWorkTimes;
    private boolean enabled;
    private Long userId;

    @Data
    @Builder
    public static class City {
        private Long id;
        private String name;
    }
}
