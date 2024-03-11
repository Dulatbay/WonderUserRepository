package kz.wonder.wonderuserrepository.entities;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(schema = "schema_wonder", name = "user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "phone_number", unique = true, nullable = false)
    private String phoneNumber;
    @Column(name = "keycloak_id", unique = true, nullable = false)
    private String keycloakId;
}
