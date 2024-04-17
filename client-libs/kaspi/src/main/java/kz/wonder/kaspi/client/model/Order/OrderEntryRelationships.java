package kz.wonder.kaspi.client.model.Order;

import com.fasterxml.jackson.annotation.JsonProperty;
import kz.wonder.kaspi.client.model.Relationship;

public class OrderEntryRelationships {
    @JsonProperty("product")
    private Relationship product;

    @JsonProperty("deliveryPointOfService")
    private Relationship deliveryPointOfService;
}

