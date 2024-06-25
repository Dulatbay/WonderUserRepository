package kz.wonder.wonderuserrepository.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static kz.wonder.wonderuserrepository.constants.ValueConstants.schemaName;

@EqualsAndHashCode(callSuper=false)
@Data
@Entity
@Table(name = "box_type", schema = schemaName)
public class BoxType extends AbstractEntity<Long> {
    @OneToMany(fetch = FetchType.LAZY, orphanRemoval = true, cascade = CascadeType.ALL, mappedBy = "boxType")
    private Set<BoxTypeImages> images = new HashSet<>();

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
    private Set<KaspiStoreAvailableBoxTypes> availableBoxTypes = new HashSet<>();

    @OneToMany(fetch = FetchType.LAZY,
            mappedBy = "boxType",
            orphanRemoval = true,
            cascade = CascadeType.ALL)
    private Set<SupplyBox> supplyBoxes = new HashSet<>();
}