package kz.wonder.wonderuserrepository.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "store_employee")
public class StoreEmployee extends AbstractEntity<Long> {
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "kaspi_store_id", nullable = false)
	private KaspiStore kaspiStore;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "wonder_user_id", nullable = false, unique = true)
	private WonderUser wonderUser;
}
