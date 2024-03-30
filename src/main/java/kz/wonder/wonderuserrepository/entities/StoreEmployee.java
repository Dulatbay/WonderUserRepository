package kz.wonder.wonderuserrepository.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import static kz.wonder.wonderuserrepository.constants.ValueConstants.schemaName;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "store_employee", schema = schemaName)
public class StoreEmployee extends AbstractEntity<Long> {
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "kaspi_store_id", nullable = false)
	private KaspiStore kaspiStore;

	@OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "wonder_user_id", nullable = false, unique = true)
	private WonderUser wonderUser;
}
