package kz.wonder.kaspi.client.model;

import lombok.Data;

@Data
public class PointOfServiceResponse {
    private String type;
    private String id;
    private DetailedAddress address = new DetailedAddress();
    private String displayName;
    private CityRelationship cityRelationship;
    private String selfLink;


    @Data
    public static class CityRelationship {
        private CityLinks links;
        private City data;
    }

    @Data
    public static class CityLinks {
        private String self;
        private String related;
    }

    @Data
    public static class City {
        private String type;
        private String id;
    }
}
