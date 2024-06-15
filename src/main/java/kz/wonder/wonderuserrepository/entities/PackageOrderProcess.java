package kz.wonder.wonderuserrepository.entities;

import jakarta.persistence.*;
import lombok.Data;

import static kz.wonder.wonderuserrepository.constants.ValueConstants.schemaName;

@Data
@Entity
@Table(name = "package_order_process", schema = schemaName)
public class PackageOrderProcess extends AbstractEntity<Long>{
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private StoreEmployee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "package_order_id", nullable = false)
    private OrderPackage orderPackage;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supply_box_product_id", nullable = false)
    private SupplyBoxProduct supplyBoxProduct;
}
