package kz.wonder.wonderuserrepository.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

import static kz.wonder.wonderuserrepository.constants.ValueConstants.schemaName;

@Data
@Entity
@Table(name = "box_type", schema = schemaName)
public class BoxType extends AbstractEntity<Long> {
    @OneToMany(fetch = FetchType.LAZY, orphanRemoval = true, cascade = CascadeType.ALL, mappedBy = "boxType")
    private List<BoxTypeImages> images;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "deleted")
    private boolean deleted;

    @OneToMany(fetch = FetchType.LAZY,
            mappedBy = "boxType",
            orphanRemoval = true,
            cascade = CascadeType.ALL)
    private List<KaspiStoreAvailableBoxTypes> availableBoxTypes;

    @OneToMany(fetch = FetchType.LAZY,
            mappedBy = "boxType",
            orphanRemoval = true,
            cascade = CascadeType.ALL)
    private List<SupplyBox> supplyBoxes;
}
