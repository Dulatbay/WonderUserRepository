package kz.wonder.wonderuserrepository.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ValueConstants {
    public static final ZoneId ZONE_ID = ZoneId.of("UTC+05:00"); // Almaty, Kazakhstan
    public static final String schemaName = "schema_wonder";
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy hh.mm");

    public static final String USER_ID_CLAIM = "user_id";
}