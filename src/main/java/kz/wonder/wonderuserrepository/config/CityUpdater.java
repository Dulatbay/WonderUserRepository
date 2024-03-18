package kz.wonder.wonderuserrepository.config;

import kz.wonder.kaspi.client.api.KaspiApi;
import kz.wonder.kaspi.client.model.CitiesDataResponse;
import kz.wonder.wonderuserrepository.entities.KaspiCity;
import kz.wonder.wonderuserrepository.repositories.KaspiCityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component
@Slf4j
public class CityUpdater {

    private final KaspiApi kaspiApi;

    private final KaspiCityRepository cityRepository;

    @Scheduled(fixedRate = 3600000 * 24) // 24 hours
    public void updateCitiesFromKaspiApi() {
        try {
            log.info("Initializing started");
            final CitiesDataResponse response = kaspiApi.getDataCities().block();
            final List<CitiesDataResponse.City> cities = response.getData();
            var count = 0;

            for (var city : cities) {
                if (!cityRepository.existsByName(city.getAttributes().getName())) {
                    final var newCity = new KaspiCity();
                    newCity.setCode(city.getAttributes().getCode());
                    newCity.setName(city.getAttributes().getName());
                    newCity.setEnabled(city.getAttributes().isActive());
                    cityRepository.save(newCity);
                    count++;
                }
            }
            log.info("Initializing ended, added {} rows", count);
        } catch (Exception e) {
            log.error("Initializing ended with error: ", e);
        }
    }
}