package kz.wonder.wonderuserrepository.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static kz.wonder.wonderuserrepository.constants.ValueConstants.schemaName;

@EqualsAndHashCode(callSuper=false)
@Data
@Entity
@Table(name = "store_employee", schema = schemaName)
public class StoreEmployee extends AbstractEntity<Long> {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kaspi_store_id", nullable = false)
    private KaspiStore kaspiStore;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, optional = false)
    @MapsId
    private WonderUser wonderUser;

    @OneToMany(fetch = FetchType.LAZY,
            mappedBy = "storeEmployee",
            orphanRemoval = true,
            cascade = CascadeType.ALL)
    private Set<StoreCellProduct> storeCellProducts = new HashSet<>();

}