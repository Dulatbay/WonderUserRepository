package kz.wonder.wonderuserrepository.entities;

import jakarta.persistence.*;
import kz.wonder.wonderuserrepository.config.converters.LocalDateTimeAttributeConverter;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

import static kz.wonder.wonderuserrepository.constants.ValueConstants.schemaName;

@EqualsAndHashCode(callSuper=false)
@Data
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
    private SupplyBoxProduct supplyBoxProduct;

    @Column(name = "finished_at", nullable = false)
    @Convert(converter = LocalDateTimeAttributeConverter.class)
    private LocalDateTime finishedAt;

    @Column(name = "started_at", nullable = false)
    @Convert(converter = LocalDateTimeAttributeConverter.class)
    private LocalDateTime startedAt;
}