package kz.wonder.wonderuserrepository.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import static kz.wonder.wonderuserrepository.constants.ValueConstants.schemaName;

@Data
@EqualsAndHashCode(callSuper=true)
@Entity
@Table(name = "order_package_process", schema = schemaName)
public class OrderPackageProcess extends AbstractEntity<Long>{
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private StoreEmployee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "package_order_id", nullable = false)
    private OrderPackage orderPackage;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "supply_box_product_id", nullable = false)
    @MapsId
    private SupplyBoxProduct supplyBoxProduct;
}
