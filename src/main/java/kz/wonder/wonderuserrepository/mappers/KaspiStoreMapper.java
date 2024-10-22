package kz.wonder.wonderuserrepository.mappers;

import kz.wonder.kaspi.client.model.DetailedAddress;
import kz.wonder.kaspi.client.model.OrdersDataResponse;
import kz.wonder.wonderuserrepository.dto.request.DayOfWeekWork;
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
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static kz.wonder.wonderuserrepository.constants.ValueConstants.TIME_FORMATTER;

@Component
@RequiredArgsConstructor
@Slf4j
public class KaspiStoreMapper {
    private final KaspiStoreRepository kaspiStoreRepository;
    private final UserService userService;
    // todo: переделать этот говно код
    @Value("${application.admin-keycloak-id}")
    private String adminKeycloakId;
    private WonderUser admin;
    private final MessageSource messageSource;

    public static String getFormattedAddress(KaspiStore kaspiStore, KaspiCity selectedCity) {
        String name = selectedCity.getName() != null ? selectedCity.getName() + ", " : "";
        String streetName = kaspiStore.getStreetName() != null ? kaspiStore.getStreetName() + ", " : "";
        String streetNumber = kaspiStore.getStreetNumber() != null ? kaspiStore.getStreetNumber() : "";
        return name + streetName + streetNumber;
    }

    public KaspiStore getKaspiStore(OrdersDataResponse.OrderAttributes orderAttributes, OrdersDataResponse.Address address, KaspiCity kaspiCity) {
        var optionalKaspiStore = kaspiStoreRepository.findByOriginAddressId(orderAttributes.getOriginAddress().getId());


        if (optionalKaspiStore.isEmpty()) {
            optionalKaspiStore = findByAddress(address.getAddress(), kaspiCity);
        }

        if (optionalKaspiStore.isEmpty()) {
            optionalKaspiStore = findByPickUpPointId(orderAttributes.getPickupPointId());
        }

        if (optionalKaspiStore.isEmpty())
            log.info("Create store with orderAttr: {}", orderAttributes.getCode());

        return optionalKaspiStore.orElseGet(() -> createStoreByParamsOfOrder(address.getId(), address.getDisplayName(), address.getAddress(), kaspiCity, orderAttributes.getPickupPointId()));

    }

    private Optional<KaspiStore> findByPickUpPointId(String pickUpPointId) {
        return kaspiStoreRepository.findByPickupPointId(pickUpPointId);
    }

    public Optional<KaspiStore> findByAddress(DetailedAddress address, KaspiCity kaspiCity) {
        Optional<KaspiStore> optionalKaspiStore;
        String streetName = address.getStreetName() == null ? null : address.getStreetName().trim();
        String streetNumber = address.getStreetNumber() == null ? null : address.getStreetNumber().trim();

        address.setStreetName(streetName);
        address.setStreetNumber(streetNumber);
        optionalKaspiStore = kaspiStoreRepository.findByStoreAddress(streetName, streetNumber, kaspiCity.getId());
        return optionalKaspiStore;
    }

    public @NonNull KaspiStore createStoreByParamsOfOrder(String addressOriginId, String displayName, DetailedAddress address, KaspiCity kaspiCity, String pickupPointId) {
        // todo: этот store создается для какого юзера(сделаю пока для main админа)
        KaspiStore kaspiStore = new KaspiStore();


        kaspiStore.setKaspiId(displayName);
        kaspiStore.setStreetName(address.getStreetName().trim());
        kaspiStore.setStreetNumber(address.getStreetNumber().trim());
        kaspiStore.setFormattedAddress(address.getFormattedAddress().trim());
        kaspiStore.setLatitude(address.getLatitude());
        kaspiStore.setLongitude(address.getLongitude());
        kaspiStore.setKaspiCity(kaspiCity);
        kaspiStore.setOriginAddressId(addressOriginId);
        kaspiStore.setPickupPointId(pickupPointId);
        kaspiStore.setComment("Generated by WONDER FBO");

        if (admin == null)
            admin = userService.getUserByKeycloakId(adminKeycloakId);

        kaspiStore.setWonderUser(admin);

        log.info("CREATED");

        return kaspiStoreRepository.save(kaspiStore);
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
        kaspiStore.setFormattedAddress(formattedAddress);
        kaspiStore.setEnabled(true);
        kaspiStore.setDeleted(false);

        return kaspiStore;
    }

    public List<KaspiStoreAvailableTimes> mapToEntity(List<DayOfWeekWork> dayOfWeekWorks, KaspiStore kaspiStore) {
        final List<KaspiStoreAvailableTimes> availableTimes = new ArrayList<>();

        dayOfWeekWorks.forEach(i -> {
            try {
                final var dayOfWeek = DayOfWeek.of(i.numericDayOfWeek());

                final var closeTime = LocalTime.parse(i.closeTime(), TIME_FORMATTER);
                final var openTime = LocalTime.parse(i.openTime(), TIME_FORMATTER);

                final var workTime = new KaspiStoreAvailableTimes();

                workTime.setDayOfWeek(dayOfWeek);
                workTime.setOpenTime(openTime);
                workTime.setCloseTime(closeTime);
                workTime.setKaspiStore(kaspiStore);

                availableTimes.add(workTime);

            } catch (DateTimeParseException e) {
                log.error("DateTimeParseException: ", e);
                throw new IllegalArgumentException(messageSource.getMessage(
                        "mappers.kaspi-store-mapper.incorrect-working-time-format",
                        null,
                        LocaleContextHolder.getLocale()
                ));
            }
        });
        return availableTimes;
    }

    public List<StoreResponse> mapToResponses(List<KaspiStore> kaspiStores) {
        return kaspiStores.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public StoreResponse mapToResponse(KaspiStore kaspiStore) {
        var city = kaspiStore.getKaspiCity();

        return StoreResponse.builder()
                .id(kaspiStore.getId())
                .kaspiId(kaspiStore.getKaspiId())
                .city(StoreResponse.City.builder()
                        .id(city.getId())
                        .name(city.getName())
                        .build())
                .streetName(kaspiStore.getStreetName())
                .streetNumber(kaspiStore.getStreetNumber())
                .formattedAddress(kaspiStore.getFormattedAddress())
                .availableWorkTimes(getAvailableTimesByStoreId(kaspiStore.getAvailableTimes()))
                .enabled(kaspiStore.isEnabled())
                .userId(kaspiStore.getWonderUser() == null ? -1 : kaspiStore.getWonderUser().getId())
                .build();
    }

    public List<StoreDetailResponse> mapToDetailResponse(List<KaspiStore> kaspiStores) {
        return kaspiStores.stream().map(this::mapToDetailResponse)
                .collect(Collectors.toList());
    }

    public StoreDetailResponse mapToDetailResponse(KaspiStore kaspiStore) {
        var city = kaspiStore.getKaspiCity();
        return StoreDetailResponse.builder()
                .id(kaspiStore.getId())
                .kaspiId(kaspiStore.getKaspiId())
                .city(StoreDetailResponse.City.builder()
                        .id(city.getId())
                        .name(city.getName())
                        .build())
                .streetName(kaspiStore.getStreetName())
                .streetNumber(kaspiStore.getStreetNumber())
                .formattedAddress(kaspiStore.getFormattedAddress())
                .availableWorkTimes(getAvailableTimesByStoreId(kaspiStore.getAvailableTimes()))
                .availableBoxTypes(kaspiStore.getAvailableBoxTypes().stream().map(
                        j -> StoreDetailResponse.AvailableBoxType.builder()
                                .id(j.getBoxType().getId())
                                .name(j.getBoxType().getName())
                                .description(j.getBoxType().getDescription())
                                .imageUrls(j.getBoxType().getImages().stream().map(k -> k.imageUrl).collect(Collectors.toList()))
                                .build()
                ).collect(Collectors.toList()))
                .enabled(kaspiStore.isEnabled())
                .userId(kaspiStore.getWonderUser() == null ? -1 : kaspiStore.getWonderUser().getId())
                .build();
    }

    private List<AvailableWorkTime> getAvailableTimesByStoreId(Set<KaspiStoreAvailableTimes> availableTimes) {

        final var awts = new ArrayList<AvailableWorkTime>();

        availableTimes.forEach(i -> {
            final var awt = AvailableWorkTime.builder()
                    .id(i.getId())
                    .openTime(i.getOpenTime().format(TIME_FORMATTER).toLowerCase())
                    .closeTime(i.getCloseTime().format(TIME_FORMATTER).toLowerCase())
                    .dayOfWeek(i.getDayOfWeek().getValue())
                    .build();

            awts.add(awt);
        });

        return awts;
    }
}
