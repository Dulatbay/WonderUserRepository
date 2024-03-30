package kz.wonder.wonderuserrepository.config.converters;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.time.DayOfWeek;

@Converter(autoApply = true)
public class DayOfWeekIntegerConverter implements AttributeConverter<DayOfWeek, Integer> {
	@Override
	public Integer convertToDatabaseColumn(DayOfWeek attribute) {
		return attribute.getValue();
	}

	@Override
	public DayOfWeek convertToEntityAttribute(Integer dbData) {
		return DayOfWeek.of(dbData);
	}
}
