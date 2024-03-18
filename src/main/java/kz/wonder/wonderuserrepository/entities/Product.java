package kz.wonder.wonderuserrepository.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import static kz.wonder.wonderuserrepository.constants.ValueConstants.schemaName;

@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name = "product", schema = schemaName)
public class Product extends AbstractEntity<Long> {
    @Column(name = "vendor_code", unique = true, nullable = false)
    private String vendorCode;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "link", nullable = false)
    private String link;

    @Column(name = "enabled", nullable = false)
    private boolean enabled;
}
