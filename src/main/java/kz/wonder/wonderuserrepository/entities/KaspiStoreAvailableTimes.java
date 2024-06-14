package kz.wonder.wonderuserrepository.entities;

import jakarta.persistence.*;
import kz.wonder.wonderuserrepository.config.converters.DayOfWeekIntegerConverter;
import lombok.Data;

import java.time.DayOfWeek;
import java.time.LocalTime;

import static kz.wonder.wonderuserrepository.constants.ValueConstants.schemaName;

@Data
@Entity
@Table(name = "kaspi_store_available_times", schema = schemaName)
public class KaspiStoreAvailableTimes extends AbstractEntity<Long> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kaspi_store", columnDefinition = "integer")
    private KaspiStore kaspiStore;

    @Column(name = "day_of_week")
    @Convert(converter = DayOfWeekIntegerConverter.class)
    private DayOfWeek dayOfWeek;

    @Column(name = "open_time")
    private LocalTime openTime;

    @Column(name = "close_time")
    private LocalTime closeTime;
}
