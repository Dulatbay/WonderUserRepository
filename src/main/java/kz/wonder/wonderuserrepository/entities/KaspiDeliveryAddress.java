package kz.wonder.wonderuserrepository.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import static kz.wonder.wonderuserrepository.constants.ValueConstants.schemaName;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "Kaspi_delivery_address", schema = schemaName)
public class KaspiDeliveryAddress extends AbstractEntity<Long> {
    private String streetName;
    private String streetNumber;
    private String town;
    private String district;
    private String building;
    private String apartment;
    private String formattedAddress;
    private double latitude;
    private double longitude;
}
