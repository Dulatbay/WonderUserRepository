package kz.wonder.wonderuserrepository.entities;

import jakarta.persistence.*;
import kz.wonder.wonderuserrepository.entities.enums.ApplicationMode;
import lombok.Data;
import lombok.EqualsAndHashCode;

import static kz.wonder.wonderuserrepository.constants.ValueConstants.schemaName;

@EqualsAndHashCode(callSuper=false)
@Data
@Entity
@Table(name = "application_property",
        schema = schemaName,
        uniqueConstraints = {@UniqueConstraint(columnNames = {"mode", "property_name"})})
public class ApplicationProperty extends AbstractEntity<Long> {
    @Column(name = "property_name", nullable = false)
    private String propertyName;

    @Column(name = "value")
    private String value;

    @Column(name = "mode", nullable = false)
    @Enumerated(EnumType.STRING)
    private ApplicationMode applicationMode;
}