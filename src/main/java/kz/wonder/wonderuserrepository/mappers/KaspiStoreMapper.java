package kz.wonder.wonderuserrepository.mappers;

import kz.wonder.kaspi.client.model.OrdersDataResponse;
import kz.wonder.wonderuserrepository.entities.KaspiCity;
import kz.wonder.wonderuserrepository.entities.KaspiStore;
import kz.wonder.wonderuserrepository.entities.WonderUser;
import kz.wonder.wonderuserrepository.repositories.KaspiStoreRepository;
import kz.wonder.wonderuserrepository.services.UserService;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KaspiStoreMapper {
    private final KaspiStoreRepository kaspiStoreRepository;
    // todo: переделать этот говно код
    @Value("${application.admin-keycloak-id}")
    private String adminKeycloakId;
    private WonderUser admin;
    private UserService userService;

    public KaspiStore getKaspiStore(OrdersDataResponse.Address address, KaspiCity kaspiCity) {
        var optionalKaspiStore = kaspiStoreRepository.findByOriginAddressId(address.getId());


        if (optionalKaspiStore.isEmpty()) {

            String apartment = address.getAddress().getApartment() == null ? null : address.getAddress().getApartment().trim();
            String streetName = address.getAddress().getStreetName() == null ? null : address.getAddress().getStreetName().trim();
            String streetNumber = address.getAddress().getStreetNumber() == null ? null : address.getAddress().getStreetNumber().trim();
            String town = address.getAddress().getTown() == null ? null : address.getAddress().getTown().trim();
            String building = address.getAddress().getBuilding() == null ? null : address.getAddress().getBuilding().trim();
            String district = address.getAddress().getDistrict() == null ? null : address.getAddress().getDistrict().trim();

            address.getAddress().setApartment(apartment);
            address.getAddress().setStreetName(streetName);
            address.getAddress().setStreetNumber(streetNumber);
            address.getAddress().setTown(town);
            address.getAddress().setBuilding(building);
            address.getAddress().setDistrict(district);

            optionalKaspiStore = kaspiStoreRepository.findByStoreAddress(apartment, streetName, streetNumber, town, building, district);
        }

        if (optionalKaspiStore.isPresent()) {
            return optionalKaspiStore.get();
        } else {
            KaspiStore kaspiStore = getStore(address, kaspiCity);
            return kaspiStoreRepository.save(kaspiStore);
        }
    }

    private @NotNull KaspiStore getStore(OrdersDataResponse.Address address, KaspiCity kaspiCity) {
        // todo: этот store создается для какого юзера(сделаю пока для main админа)
        KaspiStore kaspiStore = new KaspiStore();


        kaspiStore.setKaspiId(address.getDisplayName());
        kaspiStore.setStreetName(address.getAddress().getStreetName());
        kaspiStore.setStreetNumber(address.getAddress().getStreetNumber());
        kaspiStore.setTown(address.getAddress().getTown());
        kaspiStore.setDistrict(address.getAddress().getDistrict());
        kaspiStore.setBuilding(address.getAddress().getBuilding());
        kaspiStore.setApartment(address.getAddress().getApartment());
        kaspiStore.setFormattedAddress(address.getAddress().getFormattedAddress());
        kaspiStore.setLatitude(address.getAddress().getLatitude());
        kaspiStore.setLongitude(address.getAddress().getLongitude());
        kaspiStore.setKaspiCity(kaspiCity);
        kaspiStore.setOriginAddressId(address.getId());

        if (admin == null)
            admin = userService.getUserByKeycloakId(adminKeycloakId);

        kaspiStore.setWonderUser(admin);
        return kaspiStore;
    }

}
