package kz.wonder.wonderuserrepository.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import static kz.wonder.wonderuserrepository.constants.ValueConstants.schemaName;

@EqualsAndHashCode(callSuper=false)
@Data
@Entity
@Table(name = "product_size", schema = schemaName)
public class ProductSize extends AbstractEntity<Long> {
    @Column(name = "origin_vendor_code", nullable = false, unique = true)
    private String originVendorCode;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private WonderUser author;

    private Double weight;
    private Double height;
    private Double length;
    private Double width;
    private String comment;
}