package kz.wonder.kaspi.client.model.response;

import lombok.Data;

@Data
public class AssembleKaspiOrderResponse {
    private String type;
    private String id;
    private Attributes attributes;
    private Relationships relationships;
    private Links links;
    private Object included;

    @Data
    public static class Attributes {
        private int numberOfSpace;
        private String status;
    }

    @Data
    public static class Relationships {
        private LinkData user;
        private LinkData entries;

        @Data
        public static class LinkData {
            private Links links;
            private Object data;
        }
    }

    @Data
    public static class Links {
        private String self;
        private String related;
    }
}
