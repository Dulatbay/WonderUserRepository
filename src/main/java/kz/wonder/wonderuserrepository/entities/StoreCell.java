package kz.wonder.wonderuserrepository.entities;


import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

import static kz.wonder.wonderuserrepository.constants.ValueConstants.schemaName;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "store_cell", schema = schemaName)
public class StoreCell extends AbstractEntity<Long> {
    @Column(nullable = false)
    private Long row;

    @Column(nullable = false)
    private Long col;

    @Column(nullable = false)
    private Long cell;

    private String comment;
    private Double width;
    private Double height;
    private Double depth;

    @OneToMany(fetch = FetchType.LAZY,
            mappedBy = "storeCell",
            orphanRemoval = true,
            cascade = CascadeType.ALL)
    private List<StoreCellProduct> storeCellProducts;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kaspi_store_id", columnDefinition = "integer", nullable = false)
    private KaspiStore kaspiStore;
}
