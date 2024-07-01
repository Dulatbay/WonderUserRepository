package kz.wonder.wonderuserrepository.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ValueConstants {
    public static final ZoneId ZONE_ID = ZoneId.of("UTC+05:00"); // Almaty, Kazakhstan
    public static final String schemaName = "schema_wonder";
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    public static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    public static final String UPLOADED_FOLDER = "upload-dir/";
    public static final String XML_SCHEMA_INSTANCE = "http://www.w3.org/2001/XMLSchema-instance";
    public static final String JAXB_SCHEMA_LOCATION = "kaspiShopping http://kaspi.kz/kaspishopping.xsd";

    public static final String USER_ID_CLAIM = "user_id";
    public static final String USER_NAME_CLAIM = "name";

    public static final LocalDateTime SERVER_INIT_TIME = LocalDateTime.parse("01.04.2024 00:00", DATE_TIME_FORMATTER);
    public static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.##");


    public static final long ORDERS_INIT_DELAY = 300000L; // five minutes
    public static final long XML_INIT_DELAY = 36000000L; // five minutes
    public static final long CITIES_INIT_DELAY = 604800000L; // one week
    public static final long SYNC_USERS_DELAY = 300000L; // five minutes
    public static final long INITIAL_DELAY = 1000L;  // five minutes
    public static final String UPDATE_ORDERS_PROPERTY_NAME = "update_orders";
    public static final String SYNC_CITIES_PROPERTY_NAME = "sync_cities";
    public static final String SYNC_USERS_PROPERTY_NAME = "sync_users";
    public static final String UPDATE_XML_PROPERTY_NAME = "update_xml";
    public static final String UPDATE_ORDERS_IGNORE_TIME_PROPERTY_NAME = "update_orders_ignore_time";

    public static final String FILE_MANAGER_XML_DIR = "xml";
    public static final String FILE_MANAGER_IMAGE_DIR = "image";
    public static final String FILE_MANAGER_DOC_DIR = "docs";
    public static final String FILE_MANAGER_SUPPLY_REPORT_DIR = "supply-report";
    public static final String FILE_MANAGER_PRODUCT_BARCODE_DIR = "product-barcode";
    public static final String FILE_MANAGER_BOX_BARCODE_DIR = "box-barcode";

    public static final String FILE_MANAGER_SUPPLY_AUTHORITY_DOCUMENTS_DIR = "supply-authority-documents";
}
