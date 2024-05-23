package kz.wonder.wonderuserrepository.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import static kz.wonder.wonderuserrepository.constants.ValueConstants.schemaName;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "order_assemble", schema = schemaName)
public class OrderAssemble extends AbstractEntity<Long> {
    @Column(name = "order_id", nullable = false, unique = true)
    private Long orderId;

    @ManyToOne
    @JoinColumn(name = "started_employee_id", nullable = false)
    private StoreEmployee startedEmployee;

    @JoinColumn(name = "assemble_state", nullable = false)
    @Enumerated(EnumType.STRING)
    private AssembleState assembleState;
}
