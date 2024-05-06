package kz.wonder.wonderuserrepository.services;

import kz.wonder.wonderuserrepository.dto.response.CityResponse;
import kz.wonder.wonderuserrepository.entities.KaspiCity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CityService {
    void syncWithKaspi();

    List<CityResponse> getAllCities();

    KaspiCity getKaspiCityByName(String name);
}
