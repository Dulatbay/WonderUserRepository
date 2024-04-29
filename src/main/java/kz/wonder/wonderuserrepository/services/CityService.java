package kz.wonder.wonderuserrepository.services;

import kz.wonder.wonderuserrepository.dto.response.CityResponse;
import kz.wonder.wonderuserrepository.entities.KaspiCity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CityService {
    void syncWithKaspi();

    Page<CityResponse> getAllCities(Pageable pageable);

    KaspiCity getKaspiCityByName(String name);
}
