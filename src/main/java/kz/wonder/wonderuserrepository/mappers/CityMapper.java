package kz.wonder.wonderuserrepository.mappers;

import kz.wonder.kaspi.client.model.CitiesDataResponse;
import kz.wonder.wonderuserrepository.dto.response.CityResponse;
import kz.wonder.wonderuserrepository.entities.KaspiCity;
import org.springframework.stereotype.Component;

@Component
public class CityMapper {
    public CityResponse toResponse(KaspiCity kaspiCity) {
        CityResponse cityResponse = new CityResponse();
        cityResponse.setId(kaspiCity.getId());
        cityResponse.setName(kaspiCity.getName());
        cityResponse.setEnabled(kaspiCity.isEnabled());
        cityResponse.setCode(kaspiCity.getCode());
        return cityResponse;
    }

    public KaspiCity toEntity(CitiesDataResponse.City city){
        var newCity = new KaspiCity();
        newCity.setCode(city.getAttributes().getCode());
        newCity.setName(city.getAttributes().getName());
        newCity.setEnabled(city.getAttributes().isActive());
        return newCity;
    }
}
