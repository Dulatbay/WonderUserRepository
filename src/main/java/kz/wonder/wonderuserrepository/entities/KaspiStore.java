package kz.wonder.wonderuserrepository.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;

import static kz.wonder.wonderuserrepository.constants.ValueConstants.schemaName;

@Data
@Entity
@Table(name = "kaspi_store", schema = schemaName)
public class KaspiStore {
}
