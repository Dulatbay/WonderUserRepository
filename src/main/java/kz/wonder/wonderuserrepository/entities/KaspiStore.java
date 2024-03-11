package kz.wonder.wonderuserrepository.entities;

import jakarta.persistence.*;

@Entity
@Table(schema = "schema_wonder", name = "kaspi_token")
public class KaspiStore {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "token", unique = true, nullable = false)
    private String token;
    @Column(name = "seller_name", nullable = false)
    private String sellerName;
    private boolean enabled;
    @OneToOne
    @JoinColumn(columnDefinition = "integer", name = "userId")
    private User user;
}
