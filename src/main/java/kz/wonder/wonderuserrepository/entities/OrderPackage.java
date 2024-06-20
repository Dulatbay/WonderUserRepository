package kz.wonder.wonderuserrepository.entities;

import jakarta.persistence.*;
import kz.wonder.wonderuserrepository.entities.enums.PackageState;
import lombok.Data;
import lombok.EqualsAndHashCode;

import static kz.wonder.wonderuserrepository.constants.ValueConstants.schemaName;

@EqualsAndHashCode(callSuper=true)
@Data
@Entity
@Table(name = "order_package", schema = schemaName)
public class OrderPackage extends AbstractEntity<Long>{
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "started_employee_id", nullable = false)
    private StoreEmployee startedEmployee;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    @MapsId
    private KaspiOrder kaspiOrder;

    @JoinColumn(name = "package_state", nullable = false)
    @Enumerated(EnumType.STRING)
    private PackageState packageState;
}
