package kz.wonder.wonderuserrepository.services.impl;


import jakarta.transaction.Transactional;
import kz.wonder.wonderuserrepository.dto.request.DayOfWeekWork;
import kz.wonder.wonderuserrepository.dto.request.KaspiStoreChangeRequest;
import kz.wonder.wonderuserrepository.dto.request.KaspiStoreCreateRequest;
import kz.wonder.wonderuserrepository.dto.response.AvailableWorkTime;
import kz.wonder.wonderuserrepository.dto.response.StoreDetailResponse;
import kz.wonder.wonderuserrepository.dto.response.StoreResponse;
import kz.wonder.wonderuserrepository.entities.KaspiStore;
import kz.wonder.wonderuserrepository.entities.KaspiStoreAvailableBoxTypes;
import kz.wonder.wonderuserrepository.entities.KaspiStoreAvailableTimes;
import kz.wonder.wonderuserrepository.exceptions.DbObjectNotFoundException;
import kz.wonder.wonderuserrepository.repositories.BoxTypeRepository;
import kz.wonder.wonderuserrepository.repositories.KaspiCityRepository;
import kz.wonder.wonderuserrepository.repositories.KaspiStoreAvailableTimesRepository;
import kz.wonder.wonderuserrepository.repositories.KaspiStoreRepository;
import kz.wonder.wonderuserrepository.services.KaspiStoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static kz.wonder.wonderuserrepository.constants.ValueConstants.TIME_FORMATTER;


@Slf4j
@Service
@RequiredArgsConstructor
public class KaspiStoreServiceImpl implements KaspiStoreService {

    private final KaspiStoreRepository kaspiStoreRepository;
    private final KaspiCityRepository kaspiCityRepository;
    private final KaspiStoreAvailableTimesRepository kaspiStoreAvailableTimesRepository;
    private final BoxTypeRepository boxTypeRepository;


    @Override
    @Transactional(rollbackOn = Exception.class)
    public void createStore(final KaspiStoreCreateRequest kaspiStoreCreateRequest) {
        final KaspiStore kaspiStore = new KaspiStore();

        log.info("kaspiStoreCreateRequest.getWonderUser().getKeycloakId(): {}", kaspiStoreCreateRequest.getWonderUser().getKeycloakId());


        var oo = kaspiCityRepository.findById(kaspiStoreCreateRequest.getCityId());

	    oo.ifPresent(kaspiCity -> System.out.println(kaspiCity.getName()));

        final var selectedCity = kaspiCityRepository.findById(kaspiStoreCreateRequest.getCityId())
                .orElseThrow(
                        () -> new DbObjectNotFoundException(HttpStatus.BAD_REQUEST.getReasonPhrase(), "City doesn't exist")
                );

        final List<KaspiStoreAvailableTimes> availableTimes = mapToEntity(kaspiStoreCreateRequest.getDayOfWeekWorks(), kaspiStore);


        kaspiStore.setWonderUser(kaspiStoreCreateRequest.getWonderUser());
        kaspiStore.setKaspiCity(selectedCity);
        kaspiStore.setKaspiId(kaspiStoreCreateRequest.getKaspiId());
        kaspiStore.setName(kaspiStoreCreateRequest.getName());
        kaspiStore.setApartment(kaspiStoreCreateRequest.getApartment());
        kaspiStore.setStreet(kaspiStoreCreateRequest.getStreet());

        kaspiStoreRepository.save(kaspiStore);
        kaspiStoreAvailableTimesRepository.saveAll(availableTimes);
    }

    @Override
    public List<StoreResponse> getAllByUser(String keycloakUserId) {
        final var kaspiStores = kaspiStoreRepository.findAllByWonderUserKeycloakId(keycloakUserId);

        return mapToResponse(kaspiStores);
    }

    @Override
    public List<StoreResponse> getAll() {
        final var kaspiStores = kaspiStoreRepository.findAll();
        return mapToResponse(kaspiStores);
    }

    private List<KaspiStoreAvailableTimes> mapToEntity(List<DayOfWeekWork> dayOfWeekWorks, KaspiStore kaspiStore) {
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
                throw new IllegalArgumentException("Incorrect work time format");
            }
        });
        return availableTimes;
    }

    private List<StoreResponse> mapToResponse(List<KaspiStore> kaspiStores) {
        return kaspiStores.stream().map(i ->
                StoreResponse.builder()
                        .id(i.getId())
                        .kaspiId(i.getKaspiId())
                        .city(i.getKaspiCity().getName())
                        .street(i.getStreet())
                        .address(i.getApartment())
                        .availableWorkTimes(getAvailableTimesByStoreId(i.getAvailableTimes()))
                        .enabled(i.isEnabled())
                        .userId(i.getWonderUser() == null ? -1 : i.getWonderUser().getId())
                        .build()).collect(Collectors.toList());
    }

    private List<StoreDetailResponse> mapToDetailResponse(List<KaspiStore> kaspiStores) {
        return kaspiStores.stream().map(i ->
                        StoreDetailResponse.builder()
                                .id(i.getId())
                                .kaspiId(i.getKaspiId())
                                .city(i.getKaspiCity().getName())
                                .street(i.getStreet())
                                .address(i.getApartment())
                                .availableWorkTimes(getAvailableTimesByStoreId(i.getAvailableTimes()))
                                .availableBoxTypes(i.getAvailableBoxTypes().stream().map(
                                        j -> StoreDetailResponse.AvailableBoxType.builder()
                                                .id(j.getBoxType().getId())
                                                .name(j.getBoxType().getName())
                                                .description(j.getBoxType().getDescription())
                                                .imageUrls(j.getBoxType().getImages().stream().map(k -> k.imageUrl).collect(Collectors.toList()))
                                                .build()
                                ).collect(Collectors.toList()))
                                .enabled(i.isEnabled())
                                .userId(i.getWonderUser() == null ? -1 : i.getWonderUser().getId())
                                .build())
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id, String keycloakUserId) {
        final var kaspiStore = kaspiStoreRepository.findByWonderUserKeycloakIdAndId(keycloakUserId, id)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND.getReasonPhrase(), "Kaspi store doesn't exist"));

        kaspiStoreRepository.delete(kaspiStore);
    }

    @Override
    public void deleteById(Long id) {
        final var kaspiStore = kaspiStoreRepository.findById(id)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND.getReasonPhrase(), "Store doesn't exist"));

        kaspiStoreRepository.delete(kaspiStore);
    }

    @Override
    public void changeStore(KaspiStoreChangeRequest changeRequest, Long id) {
        final var kaspiStore = kaspiStoreRepository.findById(id)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND.getReasonPhrase(), "Store doesn't exist"));


        kaspiStoreRepository.save(mapToEntity(changeRequest, kaspiStore));
    }

    @Override
    public void changeStore(KaspiStoreChangeRequest changeRequest, Long id, String userId) {
        final var storeToDelete = kaspiStoreRepository.findByWonderUserKeycloakIdAndId(userId, id)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND.getReasonPhrase(), "Store doesn't exist"));

        kaspiStoreRepository.save(mapToEntity(changeRequest, storeToDelete));
    }

    @Override
    public void addBoxTypeToStore(Long boxTypeId, Long storeId) {
        var kaspiStore = kaspiStoreRepository.findById(storeId)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.BAD_REQUEST.getReasonPhrase(), "Kaspi store doesn't exist"));

        var boxType = boxTypeRepository.findById(boxTypeId)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.BAD_REQUEST.getReasonPhrase(), "Box type doesn't exist"));

        var availableBoxType = new KaspiStoreAvailableBoxTypes();

        availableBoxType.setBoxType(boxType);
        availableBoxType.setKaspiStore(kaspiStore);
        availableBoxType.setEnabled(true);

        kaspiStore.getAvailableBoxTypes().add(availableBoxType);

        kaspiStoreRepository.save(kaspiStore);
    }

    @Override
    public void addBoxTypeToStore(Long boxTypeId, Long storeId, String keycloakUserId) {
        var kaspiStore = kaspiStoreRepository.findByWonderUserKeycloakIdAndId(keycloakUserId, storeId)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.BAD_REQUEST.getReasonPhrase(), "Kaspi store doesn't exist"));

        var boxType = boxTypeRepository.findById(boxTypeId)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.BAD_REQUEST.getReasonPhrase(), "Box type doesn't exist"));

        var availableBoxType = new KaspiStoreAvailableBoxTypes();

        availableBoxType.setBoxType(boxType);
        availableBoxType.setKaspiStore(kaspiStore);
        availableBoxType.setEnabled(true);

        kaspiStore.getAvailableBoxTypes().add(availableBoxType);

        kaspiStoreRepository.save(kaspiStore);
    }

    @Override
    public List<StoreDetailResponse> getAllDetail() {
        return mapToDetailResponse(kaspiStoreRepository.findAll());
    }

    @Override
    public List<StoreDetailResponse> getAllDetailByUser(String keycloakId) {
        return mapToDetailResponse(kaspiStoreRepository.findAllByWonderUserKeycloakId(keycloakId));
    }

    @Override
    public void removeBoxType(Long boxTypeId, Long storeId) {
        var store = kaspiStoreRepository.findById(storeId)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.BAD_REQUEST.getReasonPhrase(), "Store doesn't exist"));

        var itemsToDelete = store.getAvailableBoxTypes()
                .stream()
                .filter(i -> i.getBoxType().getId().equals(boxTypeId))
                .toList();

        log.info("Items to delete size: {}", itemsToDelete.size());


        if (itemsToDelete.isEmpty()) {
            throw new IllegalArgumentException("Store doesn't contain box type with ID: " + boxTypeId);
        }


        store.getAvailableBoxTypes()
                .removeAll(itemsToDelete);

        kaspiStoreRepository.save(store);
    }

    @Override
    public void removeBoxType(Long boxTypeId, Long storeId, String keycloakId) {
        var store = kaspiStoreRepository.findByWonderUserKeycloakIdAndId(keycloakId, storeId)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.BAD_REQUEST.getReasonPhrase(), "Store doesn't exist"));

        store.getAvailableBoxTypes()
                .removeIf(i -> Objects.equals(i.getId(), storeId));

        kaspiStoreRepository.save(store);
    }

    private KaspiStore mapToEntity(KaspiStoreChangeRequest changeRequest, KaspiStore kaspiStore) {
        kaspiStore.setStreet(changeRequest.getStreet());
        kaspiStore.setApartment(changeRequest.getApartment());
        kaspiStore.setKaspiId(changeRequest.getKaspiId());
        kaspiStore.setName(changeRequest.getName());
        kaspiStore.setEnabled(changeRequest.isEnabled());
        kaspiStore.setKaspiCity(
                kaspiCityRepository.findById(changeRequest.getCityId())
                        .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.BAD_REQUEST.getReasonPhrase(), "City doesn't exist"))
        );
        kaspiStore.getAvailableTimes().clear();
        kaspiStore.getAvailableTimes().addAll(mapToEntity(changeRequest.getDayOfWeekWorks(), kaspiStore));
        return kaspiStore;
    }

    public List<AvailableWorkTime> getAvailableTimesByStoreId(List<KaspiStoreAvailableTimes> availableTimes) {

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
