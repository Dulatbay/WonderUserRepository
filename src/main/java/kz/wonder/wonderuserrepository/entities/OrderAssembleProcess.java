package kz.wonder.wonderuserrepository.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import static kz.wonder.wonderuserrepository.constants.ValueConstants.schemaName;

@EqualsAndHashCode(callSuper=false)
@Data
@Entity
@Table(name = "order_assemble_process", schema = schemaName, uniqueConstraints = {@UniqueConstraint(columnNames = {"order_assemble_id", "store_cell_product_id"})})
public class OrderAssembleProcess extends AbstractEntity<Long> {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_assemble_id", nullable = false)
    private OrderAssemble orderAssemble;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_employee_id", nullable = false)
    private StoreEmployee storeEmployee;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "store_cell_product_id")
    private StoreCellProduct storeCellProduct;
}
