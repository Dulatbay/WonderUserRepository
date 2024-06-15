package kz.wonder.wonderuserrepository.entities;

import jakarta.persistence.*;
import lombok.Data;

import static kz.wonder.wonderuserrepository.constants.ValueConstants.schemaName;

@Data
@Entity
@Table(name = "order_transmission", schema = schemaName)
public class OrderTransmission extends AbstractEntity<Long> {
    @OneToOne
    @JoinColumn(name = "order_id", nullable = false)
    private KaspiOrder kaspiOrder;

    @ManyToOne
    @JoinColumn(name = "started_employee_id", nullable = false)
    private StoreEmployee startedEmployee;

    @JoinColumn(name = "transmission_state", nullable = false)
    @Enumerated(EnumType.STRING)
    private OrderTransmissionState orderTransmissionState;
}
