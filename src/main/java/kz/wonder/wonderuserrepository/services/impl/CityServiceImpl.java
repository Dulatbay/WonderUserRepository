package kz.wonder.wonderuserrepository.services.impl;

import kz.wonder.kaspi.client.api.KaspiApi;
import kz.wonder.kaspi.client.model.CitiesDataResponse;
import kz.wonder.wonderuserrepository.entities.KaspiCity;
import kz.wonder.wonderuserrepository.exceptions.DbObjectNotFoundException;
import kz.wonder.wonderuserrepository.repositories.KaspiCityRepository;
import kz.wonder.wonderuserrepository.services.CityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CityServiceImpl implements CityService {
    private final KaspiApi kaspiApi;
    private final KaspiCityRepository cityRepository;

    @Override
    public void syncWithKaspi() {
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

    @Override
    public List<KaspiCity> getAllCities() {
        return cityRepository.findAll();
    }

    @Override
    public KaspiCity getKaspiCityByName(String name) {
        return cityRepository.findByName(name)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.BAD_REQUEST.getReasonPhrase(), "City doesn't exist"));
    }
}
