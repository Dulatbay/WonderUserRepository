package kz.wonder.wonderuserrepository.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class StoreDetailResponse {
    private Long id;
    private String kaspiId;
    private String streetName;
    private String streetNumber;
    private String town;
    private String district;
    private String building;
    private String apartment;
    private City city;
    private List<AvailableWorkTime> availableWorkTimes;
    private List<AvailableBoxType> availableBoxTypes;
    private boolean enabled;
    private Long userId;


    @Data
    @Builder
    public static class City {
        private Long id;
        private String name;
    }

    @Data
    @Builder
    public static class AvailableBoxType {
        private Long id;
        private String name;
        private String description;
        private List<String> imageUrls;
    }
}
