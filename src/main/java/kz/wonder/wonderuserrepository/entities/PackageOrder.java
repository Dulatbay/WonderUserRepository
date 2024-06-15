package kz.wonder.wonderuserrepository.entities;

import jakarta.persistence.*;
import lombok.Data;

import static kz.wonder.wonderuserrepository.constants.ValueConstants.schemaName;

@Data
@Entity
@Table(name = "package_order", schema = schemaName)
public class PackageOrder extends AbstractEntity<Long>{
    @ManyToOne
    @JoinColumn(name = "started_employee_id", nullable = false)
    private StoreEmployee startedEmployee;

    @OneToOne
    @JoinColumn(name = "assemble_id", nullable = false)
    private OrderAssemble orderAssemble;

    @JoinColumn(name = "package_state", nullable = false)
    @Enumerated(EnumType.STRING)
    private PackageState packageState;
}
