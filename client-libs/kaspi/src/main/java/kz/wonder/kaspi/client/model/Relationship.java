package kz.wonder.kaspi.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Relationship {
    @JsonProperty("links")
    private Link links;

    @JsonProperty("data")
    private RelationshipData data;
}

