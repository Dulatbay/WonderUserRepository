package kz.wonder.wonderuserrepository.entities;

import jakarta.persistence.*;

@Entity
@Table(schema = "schema_wonder", name = "user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String phoneNumber;
    private String keycloakId;
}
