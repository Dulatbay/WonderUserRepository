package kz.wonder.kaspi.client.model.request;

import lombok.Data;

@Data
public class KaspiOrderAssembleRequest {
    private String type;
    private String id;
    private Attributes attributes;


    @Data
    public static class Attributes {
        private String status;
        private String numberOfSpace;
    }
}
