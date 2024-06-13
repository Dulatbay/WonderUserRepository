package kz.wonder.wonderuserrepository.entities;

import jakarta.persistence.*;
import kz.wonder.wonderuserrepository.constants.Utils;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

import static kz.wonder.wonderuserrepository.constants.ValueConstants.schemaName;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "supply_box_products", schema = schemaName)
public class SupplyBoxProduct extends AbstractEntity<Long> {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supply_box_id", columnDefinition = "integer")
    private SupplyBox supplyBox;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", columnDefinition = "integer")
    private Product product;

    @Column(name = "article", nullable = false, unique = true, updatable = false)
    private String article;

    @Column(name = "path_to_barcode", nullable = false, unique = true, updatable = false)
    private String pathToBarcode;

    @Column(name = "product_state", nullable = false)
    @Enumerated(value = EnumType.STRING)
    private ProductStateInStore state;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kaspi_order_id")
    private KaspiOrder kaspiOrder;

    @Column(name = "accepted_time")
    private LocalDateTime acceptedTime;

    @OneToOne(fetch = FetchType.LAZY,
            mappedBy = "supplyBoxProduct",
            orphanRemoval = true,
            cascade = CascadeType.ALL)
    private StoreCellProduct storeCellProduct;

    @OneToOne(fetch = FetchType.LAZY,
            mappedBy = "supplyBoxProduct",
            orphanRemoval = true,
            cascade = CascadeType.ALL)
    private KaspiOrderProduct kaspiOrderProducts;

    @Override
    protected void onCreate() {
        super.onCreate();
        if (article == null) {
            article = Utils.generateRandomNumber();
            pathToBarcode = article + ".xml";
        }
    }
}
