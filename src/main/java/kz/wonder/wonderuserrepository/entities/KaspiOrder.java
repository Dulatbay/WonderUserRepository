package kz.wonder.wonderuserrepository.entities;

import jakarta.persistence.*;
import kz.wonder.kaspi.client.model.PaymentMode;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

import static kz.wonder.wonderuserrepository.constants.ValueConstants.schemaName;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "kaspi_order", schema = schemaName)
public class KaspiOrder extends AbstractEntity<Long> {


    private String kaspiId;
    private String code;
    private Double totalPrice;

    @Enumerated(value = EnumType.STRING)
    private PaymentMode paymentMode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kaspi_city_id", columnDefinition = "integer")
    private KaspiCity kaspiCity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kaspi_store_id", columnDefinition = "integer")
    private KaspiStore kaspiStore;

    private Long plannedDeliveryDate;
    private Long creationDate;
    private Double deliveryCostForSeller;
    private Boolean isKaspiDelivery;
    private String deliveryMode;
    private Boolean signatureRequired;
    private Integer creditTerm;
    private Boolean preOrder;
    private String pickupPointId;
    private String state;
    private Boolean assembled;
    private Long approvedByBankDate;
    private String status;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "kaspi_delivery_address", columnDefinition = "integer")
    private KaspiDeliveryAddress deliveryAddress;

    private String waybill;
    private Long courierTransmissionDate;
    private Long courierTransmissionPlanningDate;
    private String waybillNumber;
    private Boolean express;
    private Boolean returnedToWarehouse;
    private String firstMileCourier;

    private String customerName;
    private String customerCellPhone;
    private String customerFirstName;
    private String customerLastName;

    private Double deliveryCost;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wonder_user_id", columnDefinition = "integer")
    private WonderUser wonderUser;

    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY)
    private List<KaspiOrderProduct> products;

}
