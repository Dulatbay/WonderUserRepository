package kz.wonder.wonderuserrepository.config;

import kz.wonder.wonderuserrepository.repositories.ApplicationPropertyRepository;
import kz.wonder.wonderuserrepository.services.ApplicationPropertyService;
import kz.wonder.wonderuserrepository.services.CityService;
import kz.wonder.wonderuserrepository.services.OrderService;
import kz.wonder.wonderuserrepository.services.UserService;
import kz.wonder.wonderuserrepository.services.impl.CityServiceImpl;
import kz.wonder.wonderuserrepository.services.impl.UserServiceImpl;
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

    @Scheduled(fixedDelay  = CITIES_INIT_DURATION)
    public void updateCitiesFromKaspiApi() {
        var applicationProperty = applicationPropertyService.getApplicationPropertyByName(SYNC_CITIES_PROPERTY_NAME);

        log.info("Sync cities is started: {}", applicationProperty.getValue());

        if (applicationProperty.getValue().equals("true")) {
            cityService.syncWithKaspi();
        }
    }

    @Scheduled(fixedDelay  = SYNC_USERS_DURATION)
    public void syncUsers() {
        var applicationProperty = applicationPropertyService.getApplicationPropertyByName(SYNC_USERS_PROPERTY_NAME);

        log.info("Sync users is started: {}", applicationProperty.getValue());

        if (applicationProperty.getValue().equals("true")) {
            userService.syncUsersBetweenDBAndKeycloak();
        }
    }

    @Scheduled(fixedDelay  = ORDERS_INIT_DURATION)
    public void updateOrders() {
        var applicationProperty = applicationPropertyService.getApplicationPropertyByName(UPDATE_ORDERS_PROPERTY_NAME);

        log.info("Update orders is started: {}", applicationProperty.getValue());

        if (applicationProperty.getValue().equals("true")) {
            orderService.updateOrders();
        }
    }
}