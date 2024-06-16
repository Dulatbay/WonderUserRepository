package kz.wonder.wonderuserrepository.services.impl;

import kz.wonder.kaspi.client.api.KaspiApi;
import kz.wonder.kaspi.client.model.CitiesDataResponse;
import kz.wonder.wonderuserrepository.dto.response.CityResponse;
import kz.wonder.wonderuserrepository.entities.KaspiCity;
import kz.wonder.wonderuserrepository.exceptions.DbObjectNotFoundException;
import kz.wonder.wonderuserrepository.mappers.CityMapper;
import kz.wonder.wonderuserrepository.repositories.KaspiCityRepository;
import kz.wonder.wonderuserrepository.services.CityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
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
    private final CityMapper cityMapper;
    private final MessageSource messageSource;

    @Override
    public void syncWithKaspi() {
        try {
            log.info("Cities Initializing started");
            final CitiesDataResponse response = kaspiApi.getDataCities().block();
            final List<CitiesDataResponse.City> cities = response.getData();

            final List<KaspiCity> kaspiCities = new ArrayList<>();

            for (var city : cities) {
                var cityExistsByName = cityRepository.existsByName(city.getAttributes().getName());
                var cityExistsByCode = cityRepository.existsByCode(city.getAttributes().getCode());
                if (!cityExistsByName && !cityExistsByCode) {
                    kaspiCities.add(cityMapper.toEntity(city));
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
        return cityRepository.findAll()
                .stream()
                .map(cityMapper::toResponse)
                .toList();
    }

    @Override
    public KaspiCity getKaspiCityByName(String name) {
        return cityRepository.findByName(name)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase(), messageSource.getMessage("services-impl.city-service-impl.city-does-not-exist", null, LocaleContextHolder.getLocale())));
    }
}
