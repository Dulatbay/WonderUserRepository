package kz.wonder.wonderuserrepository.entities;


import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

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

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "kaspi_store_id", columnDefinition = "integer", nullable = false)
    private KaspiStore kaspiStore;
}
