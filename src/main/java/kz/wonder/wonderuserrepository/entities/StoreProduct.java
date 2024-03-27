package kz.wonder.wonderuserrepository.entities;

import jakarta.persistence.*;

import static kz.wonder.wonderuserrepository.constants.ValueConstants.schemaName;

@Entity
@Table(name = "store_product", schema = schemaName)
public class StoreProduct extends AbstractEntity<Long> {
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "product_id", columnDefinition = "integer")
	private Product product;
}
