package kz.wonder.wonderuserrepository.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

import static kz.wonder.wonderuserrepository.constants.ValueConstants.schemaName;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "supply", schema = schemaName)
public class Supply extends AbstractEntity<Long> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", columnDefinition = "integer")
    private User author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", columnDefinition = "integer")
    private KaspiStore kaspiStore;

    @OneToMany(fetch = FetchType.LAZY,
            mappedBy = "supply",
            orphanRemoval = true,
            cascade = CascadeType.ALL)
    private List<SupplyBox> supplyBoxes;
}
