package kz.wonder.wonderuserrepository.dto.response;

import kz.wonder.kaspi.client.model.PaymentMode;
import kz.wonder.wonderuserrepository.entities.enums.DeliveryMode;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderResponse {
    private String kaspiId;
    private String code;
    private Double totalPrice;
    private PaymentMode paymentMode;
    private Long plannedDeliveryDate;
    private Long creationDate;
    private Double deliveryCostForSeller;
    private Boolean isKaspiDelivery;
    private DeliveryMode deliveryMode;
    private Double tradePrice;
    private String state;
    private String waybill;
    private Long courierTransmissionDate;
    private Long courierTransmissionPlanningDate;
    private String waybillNumber;
    private Double deliveryCost;
    private String sellerName;
    private String storeFormattedAddress;
}
