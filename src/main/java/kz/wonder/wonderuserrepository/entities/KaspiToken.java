package kz.wonder.wonderuserrepository.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import static kz.wonder.wonderuserrepository.constants.ValueConstants.schemaName;

@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(schema = schemaName, name = "kaspi_token")
public class KaspiToken extends AbstractEntity<Long> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "token", unique = true, nullable = false)
    private String token;
    @Column(name = "seller_name", nullable = false)
    private String sellerName;
    @Column(name = "seller_id", unique = true, nullable = false)
    private String sellerId;
    @Column(name = "enabled", nullable = false)
    private boolean enabled;
    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
    @JoinColumn(columnDefinition = "integer", name = "user_id")
    private WonderUser wonderUser;
}
