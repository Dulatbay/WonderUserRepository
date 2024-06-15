package kz.wonder.kaspi.client.model.response.Order;

import com.fasterxml.jackson.annotation.JsonProperty;
import kz.wonder.kaspi.client.model.Link;
import lombok.Data;

public class OrderEntryRelationships {
    @JsonProperty("product")
    private Relationship product;

    @JsonProperty("deliveryPointOfService")
    private Relationship deliveryPointOfService;

    @Data
    public static class Relationship {
        @JsonProperty("links")
        private Link links;

        @JsonProperty("data")
        private RelationshipData data;

        @Data
        public static class RelationshipData {
            private String type;
            private String id;
        }
    }



}

