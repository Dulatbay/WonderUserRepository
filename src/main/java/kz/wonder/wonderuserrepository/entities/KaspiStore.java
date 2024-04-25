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

	@Column(name = "streetName")
	private String streetName;

	@Column(name = "origin_address_id")
	private String originAddressId;

	@Column(name = "streetNumber")
	private String streetNumber;

	@Column(name = "town")
	private String town;

	@Column(name = "district")
	private String district;

	@Column(name = "building")
	private String building;

	@Column(name = "apartment")
	private String apartment;

	@Column(name = "formattedAddress")
	private String formattedAddress;

	@Column(name = "latitude")
	private Double latitude;

	@Column(name = "longitude")
	private Double longitude;

	@Column(name = "kaspi_id", nullable = false)
	private String kaspiId;

	@Column
	private boolean enabled;

	@ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
	@JoinColumn(name = "user_id", columnDefinition = "integer")
	private WonderUser wonderUser;

	@OneToMany(fetch = FetchType.LAZY,
			mappedBy = "kaspiStore",
			orphanRemoval = true,
			cascade = CascadeType.ALL)
	private List<StoreEmployee> employees;

	@OneToMany(fetch = FetchType.LAZY,
			mappedBy = "kaspiStore",
			orphanRemoval = true,
			cascade = CascadeType.ALL)
	private List<StoreCell> storeCells;

	@OneToMany(fetch = FetchType.LAZY,
			mappedBy = "kaspiStore",
			orphanRemoval = true,
			cascade = CascadeType.ALL)
	private List<KaspiOrder> orders;

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
