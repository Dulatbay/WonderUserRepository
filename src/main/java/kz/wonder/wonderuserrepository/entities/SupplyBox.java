package kz.wonder.wonderuserrepository.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

import static kz.wonder.wonderuserrepository.constants.ValueConstants.schemaName;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "supply_box", schema = schemaName)
public class SupplyBox extends AbstractEntity<Long> {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "box_type_id", columnDefinition = "integer")
    private BoxType boxType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supply_id", columnDefinition = "integer")
    private Supply supply;

    @OneToMany(fetch = FetchType.LAZY,
            mappedBy = "supplyBox",
            orphanRemoval = true,
            cascade = CascadeType.ALL)
    private List<SupplyBoxProducts> supplyBoxProducts;
}
