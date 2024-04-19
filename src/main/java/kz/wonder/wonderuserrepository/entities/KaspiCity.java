package kz.wonder.wonderuserrepository.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minidev.json.annotate.JsonIgnore;

import java.util.List;

import static kz.wonder.wonderuserrepository.constants.ValueConstants.schemaName;

@EqualsAndHashCode(callSuper = false)
@Data
@Table(name = "kaspi_city", schema = schemaName)
@Entity
public class KaspiCity extends AbstractEntity<Long> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", unique = true)
    private String name;

    @Column(name = "code", unique = true)
    private String code;

    @Column
    private boolean enabled;

    // todo: create dto

    @JsonIgnore
    @OneToMany(fetch = FetchType.LAZY,
            mappedBy = "kaspiCity",
            orphanRemoval = true,
            cascade = CascadeType.ALL)
    private List<KaspiOrder> kaspiOrders;

}


