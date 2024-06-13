package kz.wonder.kaspi.client.model;

import lombok.Data;

@Data
public class DetailedAddress {
    private String streetName;
    private String streetNumber;
    private String town;
    private String district;
    private String building;
    private String apartment;
    private String formattedAddress;
    private Double latitude;
    private Double longitude;
}