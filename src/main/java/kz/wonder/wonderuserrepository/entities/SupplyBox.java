package kz.wonder.wonderuserrepository.entities;

import jakarta.persistence.*;
import kz.wonder.wonderuserrepository.constants.Utils;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static kz.wonder.wonderuserrepository.constants.ValueConstants.schemaName;

@EqualsAndHashCode(callSuper=false)
@Data
@Entity
@Table(name = "supply_box", schema = schemaName)
public class SupplyBox extends AbstractEntity<Long> {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "box_type_id")
    private BoxType boxType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supply_id", nullable = false)
    private Supply supply;

    @OneToMany(fetch = FetchType.LAZY,
            mappedBy = "supplyBox",
            orphanRemoval = true,
            cascade = CascadeType.ALL)
    private Set<SupplyBoxProduct> supplyBoxProducts = new HashSet<>();

    @Column(name = "vendor_code", nullable = false, unique = true)
    private String vendorCode;

    @Column(name = "path_to_barcode", nullable = false, unique = true, updatable = false)
    private String pathToBarcode;

    @Override
    protected void onCreate() {
        super.onCreate();
        if (vendorCode == null) {
            vendorCode = Utils.generateRandomNumber();
            pathToBarcode = vendorCode + ".pdf";
        }
    }
}