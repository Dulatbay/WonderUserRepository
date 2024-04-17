package kz.wonder.kaspi.client.model.Order;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class OrderEntryAttributes {
    @JsonProperty("unitType")
    private String unitType;

    @JsonProperty("offer")
    private Offer offer;

    private Integer quantity;
    private Double totalPrice;
    private Double weight;
    private Integer entryNumber;

    @JsonProperty("category")
    private Category category;

    @JsonProperty("deliveryCost")
    private Double deliveryCost;

    @JsonProperty("basePrice")
    private Double basePrice;

    @Data
    public static class Offer {
        private String code;
        private String name;
    }

    @Data
    public static class Category {
        private String code;
        private String title;
    }
}
