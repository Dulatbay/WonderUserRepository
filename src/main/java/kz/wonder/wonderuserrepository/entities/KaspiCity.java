package kz.wonder.wonderuserrepository.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.HashSet;
import java.util.Set;

import static kz.wonder.wonderuserrepository.constants.ValueConstants.schemaName;

@EqualsAndHashCode(callSuper=false)
@Data
@Table(name = "kaspi_city", schema = schemaName)
@Entity
public class KaspiCity extends AbstractEntity<Long> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "code", unique = true)
    private String code;

    @Column(name = "kaspi_id", unique = true)
    private String kaspiId;

    @Column(name = "enabled", nullable = false)
    private boolean enabled;

    @OneToMany(fetch = FetchType.LAZY,
            mappedBy = "kaspiCity",
            orphanRemoval = true,
            cascade = CascadeType.ALL)
    private Set<KaspiOrder> kaspiOrders = new HashSet<>();

    @OneToMany(fetch = FetchType.LAZY,
            mappedBy = "kaspiCity",
            orphanRemoval = true,
            cascade = CascadeType.ALL)
    private Set<KaspiStore> kaspiStores = new HashSet<>();
}