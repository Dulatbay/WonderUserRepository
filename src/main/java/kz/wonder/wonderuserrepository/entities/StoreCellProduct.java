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
    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "store_cell_id", nullable = false)
    private StoreCell storeCell;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "supply_box_product_id", nullable = false)
    private SupplyBoxProduct supplyBoxProduct;

    @OneToOne(fetch = FetchType.LAZY,
            mappedBy = "storeCellProduct",
            orphanRemoval = true,
            cascade = CascadeType.ALL)
    private OrderAssembleProcess assembleProcess;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "employee_id", nullable = false)
    private StoreEmployee storeEmployee;

    
    // todo: add not null constraint
    @Column(name = "is_busy", nullable = false)
    private boolean isBusy;
}
