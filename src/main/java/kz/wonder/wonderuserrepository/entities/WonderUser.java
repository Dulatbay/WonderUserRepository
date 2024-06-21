package kz.wonder.wonderuserrepository.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.LazyToOne;

import java.util.List;

import static kz.wonder.wonderuserrepository.constants.ValueConstants.schemaName;

@EqualsAndHashCode(callSuper=false)
@Data
@Entity
@Table(schema = schemaName, name = "wonder_user")
public class WonderUser extends AbstractEntity<Long> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "phone_number", unique = true, nullable = false)
    private String phoneNumber;

    @Column(name = "keycloak_id", unique = true, nullable = false)
    private String keycloakId;


    @Column(name = "username")
    private String username;

    @EqualsAndHashCode.Exclude
    @OneToOne(fetch = FetchType.LAZY,
            mappedBy = "wonderUser",
            cascade = CascadeType.REMOVE,
            optional = false)
    private KaspiToken kaspiToken;

    @EqualsAndHashCode.Exclude
    @OneToOne(fetch = FetchType.LAZY,
            mappedBy = "wonderUser",
            cascade = CascadeType.REMOVE,
            optional = false)
    private StoreEmployee storeEmployee;

    @EqualsAndHashCode.Exclude
    @OneToOne(fetch = FetchType.LAZY,
            mappedBy = "wonderUser",
            cascade = CascadeType.REMOVE,
            optional = false)
    private TelegramAccount telegramAccount;

    @EqualsAndHashCode.Exclude
    @OneToMany(fetch = FetchType.LAZY,
            mappedBy = "wonderUser",
            orphanRemoval = true,
            cascade = CascadeType.ALL)
    private List<KaspiStore> stores;

    @EqualsAndHashCode.Exclude
    @OneToMany(fetch = FetchType.LAZY,
            mappedBy = "wonderUser",
            orphanRemoval = true,
            cascade = CascadeType.ALL)
    private List<KaspiOrder> kaspiOrders;
}
