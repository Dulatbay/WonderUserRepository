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

    public static final String UPLOAD_AUTHORITY_DOCUMENT_OF_SUPPLY = "supply-authority-documents";

    public static final double BUBBLE_WRAP_SMALL_WIDTH = 0.31;  // meters
    public static final double BUBBLE_WRAP_SMALL_LENGTH = 50;  // meters
    public static final double BUBBLE_WRAP_SMALL_COST = 2000;  // тенге

    public static final double BUBBLE_WRAP_LARGE_WIDTH = 0.61;  // meters
    public static final double BUBBLE_WRAP_LARGE_LENGTH = 100;  // meters
    double BUBBLE_WRAP_LARGE_COST = 4000;  // тенге

    public static final double TAPE_WIDTH = 0.048;  // meters (48mm)
    public static final double TAPE_LENGTH = 132;  // meters
    public static final double TAPE_COST = 660;  // тенге

    public static final double TAPE_FRAGILE_WIDTH = 0.048;  // meters (48mm)
    public static final double TAPE_FRAGILE_LENGTH = 50;  // meters
    public static final double TAPE_FRAGILE_COST = 315;  // тенге

    public static final int MANIPULATIVE_SIGN_COUNT = 100;
    public static final double MANIPULATIVE_SIGN_COST = 1389;

    public static final double MONTHLY_SALARY = 250000;  // тенге
    public static final double WORK_HOURS_PER_WEEK = 40;  // hours
    public static final double WEEKS_PER_MONTH = 4.33;

    public static final double MAX_LENGTH = 30;  // cm

    public static final double LABEL_WIDTH = 0.075;  // meters
    public static final double LABEL_LENGTH = 0.12;  // meters
    public static final int LABELS_PER_ROLL = 300;  // labels
    public static final double LABEL_COST = 1750;  // тенге

    public static final double COURIER_PACKAGE_SIZE_CM_LENGTH = 11;  // cm
    public static final double COURIER_PACKAGE_SIZE_CM_WIDTH = 21;  // cm
    public static final int COURIER_PACKAGE_COUNT = 100;  // units per pack
    public static final double COURIER_PACKAGE_COST = 1300;  // тенге per pack

    public static final double STRETCH_FILM_WIDTH = 0.50;  // meters
    public static final double STRETCH_FILM_THICKNESS = 20;  // micrometers
    public static final double STRETCH_FILM_LENGTH = 220;  // meters
    public static final double STRETCH_FILM_COST = 4000;  // тенге per roll
}
