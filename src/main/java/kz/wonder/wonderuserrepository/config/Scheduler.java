package kz.wonder.wonderuserrepository.config;

import kz.wonder.wonderuserrepository.services.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import static kz.wonder.wonderuserrepository.constants.ValueConstants.*;

@RequiredArgsConstructor
@Component
@Slf4j
public class Scheduler {
    private final OrderService orderService;
    private final ApplicationPropertyService applicationPropertyService;
    private final CityService cityService;
    private final UserService userService;
    private final ProductService productService;

    @Scheduled(fixedRate = CITIES_INIT_DELAY, initialDelay = INITIAL_DELAY)
    public void updateCitiesFromKaspiApi() {
        var applicationProperty = applicationPropertyService.getApplicationPropertyByName(SYNC_CITIES_PROPERTY_NAME);

        log.info("Sync cities is started: {}", applicationProperty.getValue());

        if (applicationProperty.getValue().equals("true")) {
            cityService.syncWithKaspi();
        }
    }

    @Scheduled(fixedRate = SYNC_USERS_DELAY, initialDelay = INITIAL_DELAY)
    public void syncUsers() {
        var applicationProperty = applicationPropertyService.getApplicationPropertyByName(SYNC_USERS_PROPERTY_NAME);

        log.info("Sync users is started: {}", applicationProperty.getValue());

        if (applicationProperty.getValue().equals("true")) {
            userService.syncUsersBetweenDBAndKeycloak();
        }
    }

    @Scheduled(fixedRate = ORDERS_INIT_DELAY, initialDelay = INITIAL_DELAY)
    public void updateOrders() {
        var applicationProperty = applicationPropertyService.getApplicationPropertyByName(UPDATE_ORDERS_PROPERTY_NAME);

        log.info("Update orders is started: {}", applicationProperty.getValue());

        if (applicationProperty.getValue().equals("true")) {
            orderService.updateOrders();
        }
    }

    @Scheduled(fixedRate = XML_INIT_DELAY, initialDelay = INITIAL_DELAY)
    public void updateXml() {
        var applicationProperty = applicationPropertyService.getApplicationPropertyByName(UPDATE_XML_PROPERTY_NAME);

        log.info("Generating xml is started: {}", applicationProperty.getValue());

        if (applicationProperty.getValue().equals("true")) {
            productService.generateXmls();
        }
    }

}