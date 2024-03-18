package kz.wonder.kaspi.client.model;

import lombok.Data;

import java.util.List;

@Data
public class CitiesDataResponse {
    private List<City> data;
    private List<Object> included;


    @Data
    public static class City {
        private String type;
        private String id;
        private Attributes attributes;
        private Object relationships;
        private Links links;
    }

    @Data
    public static class Attributes {
        private String code;
        private String name;
        private boolean active;
    }


    @Data
    public static class Links {
        private String self;
    }
}
