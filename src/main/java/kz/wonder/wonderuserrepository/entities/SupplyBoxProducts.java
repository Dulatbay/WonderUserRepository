package kz.wonder.wonderuserrepository.entities;

import jakarta.persistence.*;
import kz.wonder.wonderuserrepository.constants.Utils;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

import static kz.wonder.wonderuserrepository.constants.ValueConstants.schemaName;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "supply_box_products", schema = schemaName)
public class SupplyBoxProducts extends AbstractEntity<Long> {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supply_box_id", columnDefinition = "integer")
    private SupplyBox supplyBox;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", columnDefinition = "integer")
    private Product product;

    @Column(name = "article", nullable = false, unique = true)
    private String article;

    @Column(name = "product_state", nullable = false)
    @Enumerated(value = EnumType.STRING)
    private ProductStateInStore state;

    @Override
    protected void onCreate() {
        super.onCreate();
        if (article == null) {
            article = Utils.generateRandomNumber();
        }
    }
}
