package kz.wonder.wonderuserrepository.entities;

import jakarta.persistence.*;
import kz.wonder.wonderuserrepository.entities.enums.KaspiProductUnitType;
import lombok.Data;
import lombok.EqualsAndHashCode;

import static kz.wonder.wonderuserrepository.constants.ValueConstants.schemaName;

@EqualsAndHashCode(callSuper=true)
@Data
@Entity
@Table(name = "kaspi_order_product", schema = schemaName)
public class KaspiOrderProduct extends AbstractEntity<Long> {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kaspi_order_id")
    private KaspiOrder order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "supply_box_product_id")
    @MapsId
    private SupplyBoxProduct supplyBoxProduct;

    @Column(name = "kaspi_id", nullable = false, unique = true)
    private String kaspiId;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "base_price", nullable = false)
    private Double basePrice;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "unit_type")
    private KaspiProductUnitType unitType;

    @Column(name = "weight")
    private Double weight;

    @Column(name = "entry_number")
    private Integer entryNumber;

    @Column(name = "delivery_cost", nullable = false)
    private Double deliveryCost;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private KaspiProductCategory category;
}
