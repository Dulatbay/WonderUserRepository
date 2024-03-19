package kz.wonder.wonderuserrepository.config;

import kz.wonder.kaspi.client.api.KaspiApi;
import kz.wonder.kaspi.client.model.CitiesDataResponse;
import kz.wonder.wonderuserrepository.entities.KaspiCity;
import kz.wonder.wonderuserrepository.repositories.KaspiCityRepository;
import kz.wonder.wonderuserrepository.services.CityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component
@Slf4j
public class CityUpdater {

    private final CityService cityService;

//    @Scheduled(fixedRate = 3600000 * 24) // 24 hours
//    public void updateCitiesFromKaspiApi() {
//       cityService.syncWithKaspi();
//    }
}