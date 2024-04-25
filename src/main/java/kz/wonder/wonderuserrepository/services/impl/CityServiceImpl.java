package kz.wonder.wonderuserrepository.services.impl;

import kz.wonder.kaspi.client.api.KaspiApi;
import kz.wonder.kaspi.client.model.CitiesDataResponse;
import kz.wonder.wonderuserrepository.dto.response.CityResponse;
import kz.wonder.wonderuserrepository.entities.KaspiCity;
import kz.wonder.wonderuserrepository.exceptions.DbObjectNotFoundException;
import kz.wonder.wonderuserrepository.repositories.KaspiCityRepository;
import kz.wonder.wonderuserrepository.services.CityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
            log.info("Cities Initializing started");
            final CitiesDataResponse response = kaspiApi.getDataCities().block();
            final List<CitiesDataResponse.City> cities = response.getData();

            final List<KaspiCity> kaspiCities = new ArrayList<>();

            for (var city : cities) {
                if (!cityRepository.existsByName(city.getAttributes().getName()) && !cityRepository.existsByCode(city.getAttributes().getCode())) {
                    final var newCity = new KaspiCity();
                    newCity.setCode(city.getAttributes().getCode());
                    newCity.setName(city.getAttributes().getName());
                    newCity.setEnabled(city.getAttributes().isActive());
                    kaspiCities.add(newCity);
                }
            }

            cityRepository.saveAll(kaspiCities);
            log.info("Cities Initializing ended, added {} rows", kaspiCities.size());
        } catch (Exception e) {
            log.error("Initializing ended with error: ", e);
        }
    }

    @Override
    public List<CityResponse> getAllCities() {
        return cityRepository.findAll().stream().map(kaspiCity -> {
            CityResponse cityResponse = new CityResponse();
            cityResponse.setId(kaspiCity.getId());
            cityResponse.setName(kaspiCity.getName());
            cityResponse.setEnabled(kaspiCity.isEnabled());
            cityResponse.setCode(kaspiCity.getCode());
            return cityResponse;
        }).toList();
    }

    @Override
    public KaspiCity getKaspiCityByName(String name) {
        return cityRepository.findByName(name)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase(), "City doesn't exist"));
    }
}
