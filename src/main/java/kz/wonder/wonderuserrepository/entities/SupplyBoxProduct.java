package kz.wonder.wonderuserrepository.entities;

import jakarta.persistence.*;
import kz.wonder.wonderuserrepository.constants.Utils;
import kz.wonder.wonderuserrepository.entities.enums.ProductStateInStore;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

import static kz.wonder.wonderuserrepository.constants.ValueConstants.schemaName;

@EqualsAndHashCode(callSuper=false)
@Data
@Entity
@Table(name = "supply_box_product", schema = schemaName)
public class SupplyBoxProduct extends AbstractEntity<Long> {
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supply_box_id")
    private SupplyBox supplyBox;

    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(name = "article", nullable = false, unique = true, updatable = false)
    private String article;

    @Column(name = "path_to_barcode", nullable = false, unique = true, updatable = false)
    private String pathToBarcode;

    @Column(name = "product_state", nullable = false)
    @Enumerated(value = EnumType.STRING)
    private ProductStateInStore state;

    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kaspi_order_id")
    private KaspiOrder kaspiOrder;

    @Column(name = "accepted_time")
    private LocalDateTime acceptedTime;

    @EqualsAndHashCode.Exclude
    @OneToOne(fetch = FetchType.LAZY,
            mappedBy = "supplyBoxProduct",
            orphanRemoval = true,
            cascade = CascadeType.ALL,
            optional = false)
    private StoreCellProduct storeCellProduct;
//
//    @OneToOne(fetch = FetchType.LAZY,
//            mappedBy = "supplyBoxProduct",
//            orphanRemoval = true,
//            cascade = CascadeType.ALL)
//    private KaspiOrderProduct kaspiOrderProduct;

    @Override
    protected void onCreate() {
        super.onCreate();
        if (article == null) {
            article = Utils.generateRandomNumber();
            pathToBarcode = article + ".pdf";
        }
    }
}