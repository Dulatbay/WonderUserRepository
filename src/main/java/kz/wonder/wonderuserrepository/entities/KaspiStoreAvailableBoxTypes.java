package kz.wonder.wonderuserrepository.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import static kz.wonder.wonderuserrepository.constants.ValueConstants.schemaName;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "kaspi_store_available_box_types", schema = schemaName)
public class KaspiStoreAvailableBoxTypes extends AbstractEntity<Long> {
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "kaspi_store", columnDefinition = "integer")
	private KaspiStore kaspiStore;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "box_type", columnDefinition = "integer")
	private BoxType boxType;

	@Column(name = "enabled", nullable = false)
	private boolean enabled;
}
