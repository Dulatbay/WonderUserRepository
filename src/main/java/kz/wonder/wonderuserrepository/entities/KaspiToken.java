package kz.wonder.wonderuserrepository.entities;

import jakarta.persistence.*;
import kz.wonder.wonderuserrepository.config.converters.LocalDateTimeAttributeConverter;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

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

    @Column(name = "path_to_xml")
    private String pathToXml;

    @Column(name = "seller_id", unique = true, nullable = false)
    private String sellerId;

    @Column(name = "enabled", nullable = false)
    private boolean enabled;

    @Column(name = "xml_updated_at")
    @Convert(converter = LocalDateTimeAttributeConverter.class)
    private LocalDateTime xmlUpdatedAt;
    
    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(columnDefinition = "integer", name = "user_id", nullable = false)
    private WonderUser wonderUser;
}
