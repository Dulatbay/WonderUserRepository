package kz.wonder.wonderuserrepository.config.converters;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.time.Instant;
import java.time.LocalDateTime;

import static kz.wonder.wonderuserrepository.constants.ValueConstants.ZONE_ID;

@Converter(autoApply = true)
public class LocalDateTimeAttributeConverter implements AttributeConverter<LocalDateTime, Long> {

    @Override
    public Long convertToDatabaseColumn(LocalDateTime locDateTime) {
        return (locDateTime == null ? null : locDateTime.atZone(ZONE_ID).toInstant().getEpochSecond());
    }

    @Override
    public LocalDateTime convertToEntityAttribute(Long sqlTimestamp) {
        return (sqlTimestamp == null ? null : LocalDateTime.ofInstant(Instant.ofEpochSecond(sqlTimestamp), ZONE_ID));
    }
}