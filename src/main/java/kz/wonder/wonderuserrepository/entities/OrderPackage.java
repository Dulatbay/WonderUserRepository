package kz.wonder.wonderuserrepository.entities;

import jakarta.persistence.*;
import kz.wonder.wonderuserrepository.entities.enums.PackageState;
import lombok.Data;

import static kz.wonder.wonderuserrepository.constants.ValueConstants.schemaName;

@Data
@Entity
@Table(name = "order_package", schema = schemaName)
public class OrderPackage extends AbstractEntity<Long>{
    @ManyToOne
    @JoinColumn(name = "started_employee_id", nullable = false)
    private StoreEmployee startedEmployee;

    @OneToOne
    @JoinColumn(name = "order_id", nullable = false)
    private KaspiOrder kaspiOrder;

    @JoinColumn(name = "package_state", nullable = false)
    @Enumerated(EnumType.STRING)
    private PackageState packageState;
}
