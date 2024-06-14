package kz.wonder.wonderuserrepository.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

import static kz.wonder.wonderuserrepository.constants.ValueConstants.schemaName;

@Data
@Entity
@Table(name = "kaspi_store", schema = schemaName)
public class KaspiStore extends AbstractEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "streetName")
    private String streetName;

    @Column(name = "origin_address_id", unique = true, nullable = false)
    private String originAddressId;

    @Column(name = "street_number")
    private String streetNumber;

    @Column(name = "formatted_address")
    private String formattedAddress;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "kaspi_id", nullable = false)
    private String kaspiId;

    @Column(name = "pickup_point_id")
    private String pickupPointId;

    @Column(name = "comment")
    private String comment;

    @Column(name = "enabled", nullable = false)
    private boolean enabled;

    @Column(name = "deleted", nullable = false)
    private boolean deleted;

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
