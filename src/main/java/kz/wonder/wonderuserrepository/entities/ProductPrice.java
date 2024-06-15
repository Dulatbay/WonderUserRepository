package kz.wonder.wonderuserrepository.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import static kz.wonder.wonderuserrepository.constants.ValueConstants.schemaName;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name = "product_price", schema = schemaName)
public class ProductPrice extends AbstractEntity<Long> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kaspi_city_id")
    private KaspiCity kaspiCity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(name = "price", nullable = false)
    private Double price;
}
