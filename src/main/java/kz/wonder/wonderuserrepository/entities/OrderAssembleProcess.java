package kz.wonder.wonderuserrepository.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import static kz.wonder.wonderuserrepository.constants.ValueConstants.schemaName;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "order_assemble_process", schema = schemaName, uniqueConstraints = {@UniqueConstraint(columnNames = {"order_assemble_id", "store_cell_product_id"})})
public class OrderAssembleProcess extends AbstractEntity<Long> {
    @ManyToOne
    @JoinColumn(name = "order_assemble_id", nullable = false)
    private OrderAssemble orderAssemble;

    @ManyToOne
    @JoinColumn(name = "store_employee_id", nullable = false)
    private StoreEmployee storeEmployee;

    @OneToOne
    @JoinColumn(name = "store_cell_product_id")
    private StoreCellProduct storeCellProduct;
}
