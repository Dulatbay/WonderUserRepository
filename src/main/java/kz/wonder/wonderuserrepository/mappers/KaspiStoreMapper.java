package kz.wonder.wonderuserrepository.mappers;

import kz.wonder.kaspi.client.model.OrdersDataResponse;
import kz.wonder.wonderuserrepository.dto.request.KaspiStoreCreateRequest;
import kz.wonder.wonderuserrepository.dto.response.AvailableWorkTime;
import kz.wonder.wonderuserrepository.dto.response.StoreDetailResponse;
import kz.wonder.wonderuserrepository.dto.response.StoreResponse;
import kz.wonder.wonderuserrepository.entities.KaspiCity;
import kz.wonder.wonderuserrepository.entities.KaspiStore;
import kz.wonder.wonderuserrepository.entities.KaspiStoreAvailableTimes;
import kz.wonder.wonderuserrepository.entities.WonderUser;
import kz.wonder.wonderuserrepository.repositories.KaspiStoreRepository;
import kz.wonder.wonderuserrepository.services.UserService;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static kz.wonder.wonderuserrepository.constants.ValueConstants.TIME_FORMATTER;

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

    public KaspiStore mapToCreateStore(KaspiStoreCreateRequest kaspiStoreCreateRequest,
                                       KaspiCity selectedCity,
                                       String formattedAddress
    ) {
        KaspiStore kaspiStore = new KaspiStore();

        kaspiStore.setWonderUser(kaspiStoreCreateRequest.getWonderUser());
        kaspiStore.setKaspiCity(selectedCity);
        kaspiStore.setKaspiId(kaspiStoreCreateRequest.getKaspiId());
        kaspiStore.setStreetName(kaspiStoreCreateRequest.getStreetName());
        kaspiStore.setStreetNumber(kaspiStoreCreateRequest.getStreetNumber());
        kaspiStore.setTown(kaspiStoreCreateRequest.getTown());
        kaspiStore.setDistrict(kaspiStoreCreateRequest.getDistrict());
        kaspiStore.setBuilding(kaspiStoreCreateRequest.getBuilding());
        kaspiStore.setApartment(kaspiStoreCreateRequest.getApartment());
        kaspiStore.setFormattedAddress(formattedAddress);
        kaspiStore.setEnabled(true);

        return kaspiStore;
    }

//    public List<AvailableWorkTime> getAvailableTimesByStoreId(List<KaspiStoreAvailableTimes> availableTimes) {
//
//        final var awts = new ArrayList<AvailableWorkTime>();
//
//        availableTimes.forEach(i -> {
//            final var awt = AvailableWorkTime.builder()
//                    .id(i.getId())
//                    .openTime(i.getOpenTime().format(TIME_FORMATTER).toLowerCase())
//                    .closeTime(i.getCloseTime().format(TIME_FORMATTER).toLowerCase())
//                    .dayOfWeek(i.getDayOfWeek().getValue())
//                    .build();
//
//            awts.add(awt);
//        });
//
//        return awts;
//    }
//
//    public StoreDetailResponse mapToDetailResponse(KaspiStore kaspiStore) {
//        var city = kaspiStore.getKaspiCity();
//        return StoreDetailResponse.builder()
//                .id(kaspiStore.getId())
//                .kaspiId(kaspiStore.getKaspiId())
//                .city(StoreDetailResponse.City.builder()
//                        .id(city.getId())
//                        .name(city.getName())
//                        .build())
//                .streetName(kaspiStore.getStreetName())
//                .streetNumber(kaspiStore.getStreetNumber())
//                .town(kaspiStore.getTown())
//                .district(kaspiStore.getDistrict())
//                .building(kaspiStore.getBuilding())
//                .apartment(kaspiStore.getApartment())
//                .address(kaspiStore.getFormattedAddress())
//                .availableWorkTimes(getAvailableTimesByStoreId(kaspiStore.getAvailableTimes()))
//                .availableBoxTypes(kaspiStore.getAvailableBoxTypes().stream().map(
//                        j -> StoreDetailResponse.AvailableBoxType.builder()
//                                .id(j.getBoxType().getId())
//                                .name(j.getBoxType().getName())
//                                .description(j.getBoxType().getDescription())
//                                .imageUrls(j.getBoxType().getImages().stream().map(k -> k.imageUrl).collect(Collectors.toList()))
//                                .build()
//                ).collect(Collectors.toList()))
//                .enabled(kaspiStore.isEnabled())
//                .userId(kaspiStore.getWonderUser() == null ? -1 : kaspiStore.getWonderUser().getId())
//                .build();
//    }
//
//    public StoreResponse mapToStoreResponse(KaspiStore kaspiStore) {
//        var city = kaspiStore.getKaspiCity();
//
//        return StoreResponse.builder()
//                .id(kaspiStore.getId())
//                .kaspiId(kaspiStore.getKaspiId())
//                .city(StoreResponse.City.builder()
//                        .id(city.getId())
//                        .name(city.getName())
//                        .build())
//                .streetName(kaspiStore.getStreetName())
//                .streetNumber(kaspiStore.getStreetNumber())
//                .town(kaspiStore.getTown())
//                .district(kaspiStore.getDistrict())
//                .building(kaspiStore.getBuilding())
//                .apartment(kaspiStore.getBuilding())
//                .apartment(kaspiStore.getApartment())
//                .formattedAddress(kaspiStore.getFormattedAddress())
//                .availableWorkTimes(getAvailableTimesByStoreId(kaspiStore.getAvailableTimes()))
//                .enabled(kaspiStore.isEnabled())
//                .userId(kaspiStore.getWonderUser() == null ? -1 : kaspiStore.getWonderUser().getId())
//                .build();
//    }

}
