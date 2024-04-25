package kz.wonder.wonderuserrepository.config;

import kz.wonder.wonderuserrepository.repositories.KaspiTokenRepository;
import kz.wonder.wonderuserrepository.services.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;

import static kz.wonder.wonderuserrepository.constants.ValueConstants.ORDERS_INIT_DURATION;

@RequiredArgsConstructor
@Component
@Slf4j
public class Scheduler {
    private final KaspiTokenRepository kaspiTokenRepository;
    //    private final CityService cityService;
    private final OrderService orderService;


//    @Scheduled(fixedRate = CITIES_INIT_DURATION)
//    public void updateCitiesFromKaspiApi() {
//        cityService.syncWithKaspi();
//    }

    @Scheduled(fixedRate = ORDERS_INIT_DURATION)
    public void updateOrders() {
        try {
            log.info("Updating orders started");
            long currentTime = System.currentTimeMillis();
            long oneDay = Duration.ofDays(1).toMillis(); // 1 day
            long startDate = currentTime - oneDay;

            var tokens = kaspiTokenRepository.findAll();

            log.info("Found {} tokens", tokens.size());

            tokens.forEach(token -> {
                try {
                    orderService.processTokenOrders(token, startDate, currentTime);
                } catch (Exception ex) {
                    log.error("Error processing orders for token: {}", token, ex);
                }
            });
        } catch (Exception ex) {
            log.error("Error updating orders", ex);
        }
    }


}