package kz.wonder.wonderuserrepository.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

import static kz.wonder.wonderuserrepository.constants.ValueConstants.schemaName;

@EqualsAndHashCode(callSuper = false)
@Data
@Entity
@Table(name = "kaspi_store", schema = schemaName)
public class KaspiStore extends AbstractEntity<Long> {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "street", nullable = false)
	private String street;

	@Column(name = "apartment", nullable = false)
	private String apartment;

	@Column(name = "kaspi_id", nullable = false)
	private String kaspiId;

	@Column
	private boolean enabled;


	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", columnDefinition = "integer")
	private WonderUser wonderUser;

	@OneToMany(fetch = FetchType.LAZY,
			mappedBy = "kaspiStore",
			orphanRemoval = true,
			cascade = CascadeType.ALL)
	private List<StoreEmployee> employees;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "kaspi_city_id", columnDefinition = "integer")
	private KaspiCity kaspiCity;

	@OneToMany(fetch = FetchType.EAGER,
			mappedBy = "kaspiStore",
			orphanRemoval = true,
			cascade = CascadeType.ALL)
	private List<KaspiStoreAvailableTimes> availableTimes;

	@OneToMany(fetch = FetchType.LAZY,
			mappedBy = "kaspiStore",
			orphanRemoval = true,
			cascade = CascadeType.ALL)
	private List<KaspiStoreAvailableBoxTypes> availableBoxTypes;

	@OneToMany(fetch = FetchType.LAZY,
			mappedBy = "kaspiStore",
			orphanRemoval = true,
			cascade = CascadeType.ALL)
	private List<Supply> supplies;
}
