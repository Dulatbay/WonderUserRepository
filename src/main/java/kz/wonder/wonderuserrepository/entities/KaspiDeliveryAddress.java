package kz.wonder.wonderuserrepository.entities;

import jakarta.persistence.Column;
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
    @Column(name = "street_name")
    private String streetName;

    @Column(name = "street_mumber")
    private String streetNumber;

    @Column(name = "town")
    private String town;

    @Column(name = "district")
    private String district;

    @Column(name = "building")
    private String building;

    @Column(name = "apartment")
    private String apartment;

    @Column(name = "formatted_address")
    private String formattedAddress;

    @Column(name = "latitude")
    private double latitude;

    @Column(name = "longitude")
    private double longitude;
}
