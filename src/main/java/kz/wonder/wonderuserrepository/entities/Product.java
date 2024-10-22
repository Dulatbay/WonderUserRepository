package kz.wonder.wonderuserrepository.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

import static kz.wonder.wonderuserrepository.constants.ValueConstants.schemaName;

@EqualsAndHashCode(callSuper=false)
@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name = "product", schema = schemaName)
public class Product extends AbstractEntity<Long> {
    @Column(name = "vendor_code", nullable = false)
    private String vendorCode;

    @Column(name = "original_vendor_code")
    private String originalVendorCode;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "link")
    private String link;

    @Column(name = "enabled", nullable = false)
    private boolean enabled;

    @Column(name = "deleted", nullable = false)
    private boolean deleted;

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @EqualsAndHashCode.Exclude
    private Set<ProductPrice> prices = new HashSet<>();

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "main_city_price_id")
    @EqualsAndHashCode.Exclude
    private ProductPrice mainCityPrice;

    @Column(name = "keycloak_id", nullable = false)
    private String keycloakId;

    @Column(name = "trade_price")
    private Double tradePrice;

    @OneToMany(fetch = FetchType.LAZY,
            mappedBy = "product",
            orphanRemoval = true,
            cascade = CascadeType.ALL)
    @EqualsAndHashCode.Exclude
    private Set<SupplyBoxProduct> supplyBoxProducts = new HashSet<>();
}