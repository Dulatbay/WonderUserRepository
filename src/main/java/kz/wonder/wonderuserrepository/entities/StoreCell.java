package kz.wonder.wonderuserrepository.entities;


import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

import static kz.wonder.wonderuserrepository.constants.ValueConstants.schemaName;

@Data
@Entity
@Table(name = "store_cell", schema = schemaName)
public class StoreCell extends AbstractEntity<Long> {
    private static final String CELL_CODE_FORMAT = "%s%03d%03d%03d";
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

    private String code;

    @Column(nullable = false)
    private boolean deleted;

    @OneToMany(fetch = FetchType.LAZY,
            mappedBy = "storeCell",
            orphanRemoval = true,
            cascade = CascadeType.ALL)
    private List<StoreCellProduct> storeCellProducts;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kaspi_store_id", nullable = false)
    private KaspiStore kaspiStore;

    @Override
    protected void onCreate() {
        super.onCreate();

        updateCode();
    }

    @Override
    protected void onUpdate() {
        super.onUpdate();
        updateCode();
    }


    private void updateCode() {
        code = String.format(CELL_CODE_FORMAT, kaspiStore.getKaspiId(), row, col, cell);
    }
}
