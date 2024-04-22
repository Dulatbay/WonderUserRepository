package kz.wonder.wonderuserrepository.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import static kz.wonder.wonderuserrepository.constants.ValueConstants.schemaName;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "kaspi_order_product", schema = schemaName)
public class KaspiOrderProduct extends AbstractEntity<Long> {
    @ManyToOne
    @JoinColumn(name = "kaspi_order_id")
    private KaspiOrder order;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne
    @JoinColumn(name = "supply_box_product")
    private SupplyBoxProduct supplyBoxProduct;

    @Column
    private Integer quantity;
}
