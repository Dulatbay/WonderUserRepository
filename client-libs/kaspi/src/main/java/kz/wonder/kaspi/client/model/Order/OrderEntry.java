package kz.wonder.kaspi.client.model.Order;

import com.fasterxml.jackson.annotation.JsonProperty;
import kz.wonder.kaspi.client.model.Link;
import lombok.Data;

@Data
public class OrderEntry {
    private String type;
    private String id;

    @JsonProperty("attributes")
    private OrderEntryAttributes attributes;

    @JsonProperty("relationships")
    private OrderEntryRelationships relationships;

    @JsonProperty("links")
    private Link links;
}
