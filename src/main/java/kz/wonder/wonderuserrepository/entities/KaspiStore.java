package kz.wonder.wonderuserrepository.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static kz.wonder.wonderuserrepository.constants.ValueConstants.schemaName;

@EqualsAndHashCode(callSuper=true)
@Data
@Entity
@Table(name = "kaspi_store", schema = schemaName)
public class KaspiStore extends AbstractEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "street_name")
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private WonderUser wonderUser;

    @OneToMany(fetch = FetchType.LAZY,
            mappedBy = "kaspiStore",
            orphanRemoval = true,
            cascade = CascadeType.ALL)
    private Set<StoreEmployee> employees = new HashSet<>();

    @OneToMany(fetch = FetchType.LAZY,
            mappedBy = "kaspiStore",
            orphanRemoval = true,
            cascade = CascadeType.ALL)
    private Set<StoreCell> storeCells = new HashSet<>();

    @OneToMany(fetch = FetchType.LAZY,
            mappedBy = "kaspiStore",
            orphanRemoval = true,
            cascade = CascadeType.ALL)
    private Set<KaspiOrder> orders = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kaspi_city_id")
    private KaspiCity kaspiCity;

    @OneToMany(
            fetch = FetchType.LAZY,
            mappedBy = "kaspiStore",
            orphanRemoval = true,
            cascade = CascadeType.ALL)
    private Set<KaspiStoreAvailableTimes> availableTimes = new HashSet<>();

    @OneToMany(
            fetch = FetchType.LAZY,
            mappedBy = "kaspiStore",
            orphanRemoval = true,
            cascade = CascadeType.ALL)
    private Set<KaspiStoreAvailableBoxTypes> availableBoxTypes = new HashSet<>();

    @OneToMany(fetch = FetchType.LAZY,
            mappedBy = "kaspiStore",
            orphanRemoval = true,
            cascade = CascadeType.ALL)
    private Set<Supply> supplies = new HashSet<>();
}
