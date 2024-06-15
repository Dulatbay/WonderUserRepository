package kz.wonder.wonderuserrepository.entities;

import jakarta.persistence.*;
import kz.wonder.wonderuserrepository.entities.enums.SupplyState;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

import static kz.wonder.wonderuserrepository.constants.ValueConstants.schemaName;

@Data
@Entity
@Table(name = "supply", schema = schemaName)
public class Supply extends AbstractEntity<Long> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private WonderUser author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    private KaspiStore kaspiStore;

    @Column(name = "supply_states", nullable = false)
    @Enumerated(value = EnumType.STRING)
    private SupplyState supplyState;

    @Column(name = "comment")
    private String comment;

    @Column(name = "accepted_time")
    private LocalDateTime acceptedTime;

    @Column(name = "selected_time", nullable = false)
    private LocalDateTime selectedTime;

    @OneToMany(fetch = FetchType.LAZY,
            mappedBy = "supply",
            orphanRemoval = true,
            cascade = CascadeType.ALL)
    private List<SupplyBox> supplyBoxes;
}
