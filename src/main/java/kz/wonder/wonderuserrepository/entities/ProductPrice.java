package kz.wonder.wonderuserrepository.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import static kz.wonder.wonderuserrepository.constants.ValueConstants.schemaName;

@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name = "product_price", schema = schemaName)
public class ProductPrice extends AbstractEntity<Long> {

    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY,
            optional = false)
    @JoinColumn(name = "kaspi_city_id")
    private KaspiCity kaspiCity;

    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY,
            optional = false)
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(name = "price", nullable = false)
    private Double price;
}