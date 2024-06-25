package kz.wonder.wonderuserrepository.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import static kz.wonder.wonderuserrepository.constants.ValueConstants.schemaName;

@EqualsAndHashCode(callSuper=true)
@Data
@Entity
@Table(name = "kaspi_store_available_box_types", schema = schemaName)
public class KaspiStoreAvailableBoxTypes extends AbstractEntity<Long> {

    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kaspi_store")
    private KaspiStore kaspiStore;

    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "box_type")
    private BoxType boxType;

    @Column(name = "enabled", nullable = false)
    private boolean enabled;
}