package kz.wonder.wonderuserrepository.entities;


import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import static kz.wonder.wonderuserrepository.constants.ValueConstants.schemaName;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "store_cell_product", schema = schemaName)
public class StoreCellProduct extends AbstractEntity<Long> {
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "kaspi_store_id", columnDefinition = "integer", nullable = false)
    private StoreCell storeCell;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "supply_product_id", columnDefinition = "integer", nullable = false)
    private SupplyBoxProduct supplyBoxProduct;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "employee_id", columnDefinition = "integer", nullable = false)
    private StoreEmployee storeEmployee;


}
