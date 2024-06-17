package kz.wonder.wonderuserrepository.entities;

import jakarta.persistence.*;
import kz.wonder.kaspi.client.model.PaymentMode;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

import static kz.wonder.wonderuserrepository.constants.ValueConstants.schemaName;

@Data
@Entity
@Table(name = "kaspi_order", schema = schemaName)
public class KaspiOrder extends AbstractEntity<Long> {


    @Column(name = "kaspi_id")
    private String kaspiId;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(name = "total_price")
    private Double totalPrice;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "payment_mode")
    private PaymentMode paymentMode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kaspi_city_id", columnDefinition = "integer")
    private KaspiCity kaspiCity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kaspi_store_id", columnDefinition = "integer")
    private KaspiStore kaspiStore;

    @Column(name = "planned_delivery_date")
    private Long plannedDeliveryDate;

    @Column(name = "creation_date")
    private Long creationDate;

    @Column(name = "delivery_cost_for_seller")
    private Double deliveryCostForSeller;

    @Column(name = "is_kaspi_delivery")
    private Boolean isKaspiDelivery;

    @Column(name = "delivery_mode")
    @Enumerated(value = EnumType.STRING)
    private DeliveryMode deliveryMode;

    @Column(name = "signature_required")
    private Boolean signatureRequired;

    @Column(name = "credit_term")
    private Integer creditTerm;

    @Column(name = "pre_order")
    private Boolean preOrder;

    @Column(name = "pickup_point_id")
    private String pickupPointId;

    @Column(name = "state")
    private String state;

    @Column(name = "assembled")
    private Boolean assembled;

    @Column(name = "approved_by_bank_date")
    private Long approvedByBankDate;

    @Column(name = "status")
    private String status;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "kaspi_delivery_address", columnDefinition = "integer")
    private KaspiDeliveryAddress deliveryAddress;


    @Column(name = "waybill")
    private String waybill;

    @Column(name = "courier_transmission_date")
    private Long courierTransmissionDate;

    @Column(name = "courier_transmission_planning_date")
    private Long courierTransmissionPlanningDate;

    @Column(name = "waybill_number")
    private String waybillNumber;

    @Column(name = "express")
    private Boolean express;

    @Column(name = "returned_to_warehouse")
    private Boolean returnedToWarehouse;

    @Column(name = "first_mile_courier")
    private String firstMileCourier;

    @OneToOne(fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    @JoinColumn(name = "customer_id",
            columnDefinition = "string",
            unique = true)
    private Customer customer;

    @Column(name = "delivery_cost")
    private Double deliveryCost;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wonder_user_id", columnDefinition = "bigint")
    private WonderUser wonderUser;

    @OneToMany(mappedBy = "order", fetch = FetchType.EAGER)
    private List<KaspiOrderProduct> products = new ArrayList<>();

    @OneToOne(fetch = FetchType.LAZY,
            mappedBy = "kaspiOrder",
            orphanRemoval = true,
            cascade = CascadeType.ALL)
    private OrderAssemble orderAssemble;

}
