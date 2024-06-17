package kz.wonder.wonderuserrepository.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;

import static kz.wonder.wonderuserrepository.constants.ValueConstants.schemaName;

@Data
@Entity
@Table(name = "customer", schema = schemaName)
public class Customer extends AbstractEntity<Long> {
    @Column(name = "kaspi_id", nullable = false, unique = true)
    private String kaspiId;

    @Column(name = "name")
    private String name;

    @Column(name = "cell_phone")
    private String cellPhone;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;
}
