package kz.wonder.wonderuserrepository.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

import static kz.wonder.wonderuserrepository.constants.ValueConstants.schemaName;

@Data
@EqualsAndHashCode(callSuper = false)
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


    @OneToOne(fetch = FetchType.LAZY,
            mappedBy = "wonderUser",
            orphanRemoval = true,
            cascade = CascadeType.ALL)
    private KaspiToken kaspiToken;

    @OneToOne(fetch = FetchType.LAZY,
            mappedBy = "wonderUser",
            orphanRemoval = true,
            cascade = CascadeType.ALL)
    private StoreEmployee storeEmployee;

    @OneToOne(fetch = FetchType.LAZY,
            mappedBy = "wonderUser",
            orphanRemoval = true,
            cascade = CascadeType.ALL)
    private TelegramAccount telegramAccount;

    @OneToMany(fetch = FetchType.LAZY,
            mappedBy = "wonderUser",
            orphanRemoval = true,
            cascade = CascadeType.ALL)
    private List<KaspiStore> stores;

    @OneToMany(fetch = FetchType.LAZY,
            mappedBy = "wonderUser",
            orphanRemoval = true,
            cascade = CascadeType.ALL)
    private List<KaspiOrder> kaspiOrders;
}
