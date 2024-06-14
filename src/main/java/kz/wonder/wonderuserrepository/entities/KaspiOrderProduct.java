package kz.wonder.wonderuserrepository.entities;

import jakarta.persistence.*;
import lombok.Data;

import static kz.wonder.wonderuserrepository.constants.ValueConstants.schemaName;

@Data
@Entity
@Table(name = "kaspi_order_product", schema = schemaName)
public class KaspiOrderProduct extends AbstractEntity<Long> {
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "kaspi_order_id")
    private KaspiOrder order;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @OneToOne
    @JoinColumn(name = "supply_box_product_id")
    private SupplyBoxProduct supplyBoxProduct;

    @Column(name = "kaspi_id", nullable = false, unique = true)
    private String kaspiId;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;
}
