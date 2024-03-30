package kz.wonder.wonderuserrepository.services;

import kz.wonder.wonderuserrepository.entities.KaspiCity;

import java.util.List;

public interface CityService {
	void syncWithKaspi();

	List<KaspiCity> getAllCities();

	KaspiCity getKaspiCityByName(String name);
}
