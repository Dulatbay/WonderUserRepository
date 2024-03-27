package kz.wonder.wonderuserrepository.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

import static kz.wonder.wonderuserrepository.constants.ValueConstants.schemaName;

@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name = "product", schema = schemaName)
public class Product extends AbstractEntity<Long> {
    @Column(name = "vendor_code", nullable = false)
    private String vendorCode;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "link", nullable = false)
    private String link;

    @Column(name = "enabled", nullable = false)
    private boolean enabled;

    @Column(name = "deleted", nullable = false)
    private boolean deleted;

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
    private List<ProductPrice> prices;

    @Column(name = "keycloak_id", nullable = false)
    private String keycloakId;

    @OneToMany(fetch = FetchType.LAZY,
            mappedBy = "product",
            orphanRemoval = true,
            cascade = CascadeType.ALL)
    private List<SupplyBoxProducts> supplyBoxes;

    @OneToMany(fetch = FetchType.LAZY,
            mappedBy = "product",
            orphanRemoval = true,
            cascade = CascadeType.ALL)
    private List<StoreProduct> storeProducts;

    @OneToMany(fetch = FetchType.LAZY,
            mappedBy = "product",
            orphanRemoval = true,
            cascade = CascadeType.ALL)
    private List<ProductQuantity> productQuantities;
}
