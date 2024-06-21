package kz.wonder.wonderuserrepository.entities;

import jakarta.persistence.*;
import kz.wonder.wonderuserrepository.entities.enums.AssembleState;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

import static kz.wonder.wonderuserrepository.constants.ValueConstants.schemaName;

@EqualsAndHashCode(callSuper=false)
@Data
@Entity
@Table(name = "order_assemble", schema = schemaName)
public class OrderAssemble extends AbstractEntity<Long> {
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private KaspiOrder kaspiOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "started_employee_id", nullable = false)
    private StoreEmployee startedEmployee;

    @JoinColumn(name = "assemble_state", nullable = false)
    @Enumerated(EnumType.STRING)
    private AssembleState assembleState;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderAssembleProcess> orderAssembleProcesses;
}
