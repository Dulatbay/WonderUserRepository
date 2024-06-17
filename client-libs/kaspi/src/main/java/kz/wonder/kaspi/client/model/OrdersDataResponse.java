package kz.wonder.kaspi.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import kz.wonder.kaspi.client.model.Order.OrderEntry;
import lombok.Data;

import java.util.List;

@Data
public class OrdersDataResponse {

    @Data
    public static class OrdersDataItem {
        private String type;

        @JsonProperty("id")
        private String orderId;

        @JsonProperty("attributes")
        private OrderAttributes attributes;
    }

    @Data
    public static class OrderAttributes {
        private String code;
        private Double totalPrice;
        private PaymentMode paymentMode;
        private Address originAddress;
        private Long plannedDeliveryDate;
        private Long creationDate;
        private Double deliveryCostForSeller;
        private Boolean isKaspiDelivery;
        private String deliveryMode;
        private DeliveryAddress deliveryAddress = new DeliveryAddress();
        private Boolean signatureRequired;
        private Integer creditTerm;
        private KaspiDelivery kaspiDelivery = new KaspiDelivery();
        private Boolean preOrder;
        private String pickupPointId;
        private String state;
        private Boolean assembled;
        private Long approvedByBankDate;
        private String status;
        private Customer customer;
        private Double deliveryCost;
    }

    @Data
    public static class Address {
        private String id;
        private String displayName;
        private DetailedAddress address;
        private City city;
    }


    @Data
    public static class City {
        private String id;
        private String code;
        private String name;
        private Boolean active;
    }

    @Data
    public static class KaspiDelivery {
        private String waybill;
        private Long courierTransmissionDate;
        private Long courierTransmissionPlanningDate;
        private String waybillNumber;
        private Boolean express;
        private Boolean returnedToWarehouse;
        private String firstMileCourier;
    }

    @Data
    public static class Customer {
        private String id;
        private String kaspiId;
        private String name;
        private String cellPhone;
        private String firstName;
        private String lastName;
    }


    @Data
    public static class Metadata {
        private Integer pageCount;
        private Integer totalCount;
    }




    @JsonProperty("data")
    private List<OrdersDataItem> data;
    @JsonProperty("included")
    private List<OrderEntry> included;
    @JsonProperty("meta")
    private Metadata meta;

}
