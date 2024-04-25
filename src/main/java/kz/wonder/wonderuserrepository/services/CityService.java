package kz.wonder.wonderuserrepository.services;

import kz.wonder.wonderuserrepository.dto.response.CityResponse;
import kz.wonder.wonderuserrepository.entities.KaspiCity;

import java.util.List;

public interface CityService {
    void syncWithKaspi();

    List<CityResponse> getAllCities();

    KaspiCity getKaspiCityByName(String name);
}
