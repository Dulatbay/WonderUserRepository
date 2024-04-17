package kz.wonder.wonderuserrepository.config;

import kz.wonder.kaspi.client.api.KaspiApi;
import kz.wonder.kaspi.client.model.OrderState;
import kz.wonder.wonderuserrepository.services.CityService;
import kz.wonder.wonderuserrepository.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
@Slf4j
public class Scheduler {

    private final CityService cityService;
    private final KaspiApi kaspiApi;
    private final static String token = "1tOPbN07ZTNE5CO7XC+foBqdKmRKmHwr4i1Z0tkUT7c=";

//    @Scheduled(fixedRate = 3600000 * 24) // 24 hours
//    public void updateCitiesFromKaspiApi() {
//       cityService.syncWithKaspi();
//    }

    @Scheduled(fixedRate = 36000 * 10)
    public void updateOrders() {
        System.out.println("Updating orders started");

        long currentTime = System.currentTimeMillis();
        long tenHoursInMillis = 36000 * 1000;
        long startDate = currentTime - tenHoursInMillis;

        kaspiApi.getOrders(token, startDate, currentTime, OrderState.KASPI_DELIVERY, 0, 100)
                .subscribe(ordersDataResponse -> {
                    System.out.println("Size of response: " + ordersDataResponse.getData().size());
                }, error -> {
                    error.printStackTrace();
                    System.err.println("Error updating orders: " + error.getMessage());
                });



    }
}