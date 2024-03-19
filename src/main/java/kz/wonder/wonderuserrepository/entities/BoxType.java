package kz.wonder.wonderuserrepository.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

import static kz.wonder.wonderuserrepository.constants.ValueConstants.schemaName;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "box_type", schema = schemaName)
public class BoxType extends AbstractEntity<Long> {
    @Column(name = "name", nullable = false, unique = true)
    private String name;
    @Column(name = "description", nullable = false)
    private String description;

    @OneToMany(fetch = FetchType.LAZY, orphanRemoval = true, cascade = CascadeType.ALL, mappedBy = "boxType")
    List<BoxTypeImages> images;

    @OneToMany(fetch = FetchType.LAZY,
            mappedBy = "boxType",
            orphanRemoval = true,
            cascade = CascadeType.ALL)
    private List<KaspiStoreAvailableBoxTypes> availableBoxTypes;
}
