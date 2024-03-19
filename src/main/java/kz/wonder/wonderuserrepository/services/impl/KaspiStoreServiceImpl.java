package kz.wonder.wonderuserrepository.services.impl;


import jakarta.transaction.Transactional;
import kz.wonder.wonderuserrepository.dto.request.DayOfWeekWork;
import kz.wonder.wonderuserrepository.dto.request.KaspiStoreChangeRequest;
import kz.wonder.wonderuserrepository.dto.request.KaspiStoreCreateRequest;
import kz.wonder.wonderuserrepository.dto.response.StoreResponse;
import kz.wonder.wonderuserrepository.entities.KaspiStore;
import kz.wonder.wonderuserrepository.entities.KaspiStoreAvailableTimes;
import kz.wonder.wonderuserrepository.exceptions.DbObjectNotFoundException;
import kz.wonder.wonderuserrepository.repositories.KaspiCityRepository;
import kz.wonder.wonderuserrepository.repositories.KaspiStoreAvailableTimesRepository;
import kz.wonder.wonderuserrepository.repositories.KaspiStoreRepository;
import kz.wonder.wonderuserrepository.repositories.UserRepository;
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
import java.util.stream.Collectors;

import static kz.wonder.wonderuserrepository.constants.ValueConstants.TIME_FORMATTER;


@Slf4j
@Service
@RequiredArgsConstructor
public class KaspiStoreServiceImpl implements KaspiStoreService {

    private final KaspiStoreRepository kaspiStoreRepository;
    private final KaspiCityRepository kaspiCityRepository;
    private final KaspiStoreAvailableTimesRepository kaspiStoreAvailableTimesRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void createStore(final KaspiStoreCreateRequest kaspiStoreCreateRequest) {
        final KaspiStore kaspiStore = new KaspiStore();

        log.info("kaspiStoreCreateRequest.getUser().getKeycloakId(): {}", kaspiStoreCreateRequest.getUser().getKeycloakId());

        final var selectedCity = kaspiCityRepository.findById(kaspiStoreCreateRequest.getCityId())
                .orElseThrow(
                        () -> new DbObjectNotFoundException(HttpStatus.BAD_REQUEST.getReasonPhrase(), "City doesn't exist")
                );

        final List<KaspiStoreAvailableTimes> availableTimes = mapToEntity(kaspiStoreCreateRequest.getDayOfWeekWorks(), kaspiStore);


        kaspiStore.setUser(kaspiStoreCreateRequest.getUser());
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
        final var kaspiStores = kaspiStoreRepository.findAllByUserKeycloakId(keycloakUserId);

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
                        .userId(i.getUser() == null ? -1 : i.getUser().getId())
                        .build()).collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id, String keycloakUserId) {
        final var kaspiStore = kaspiStoreRepository.findByUserKeycloakIdAndId(keycloakUserId, id)
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
        final var storeToDelete = kaspiStoreRepository.findByUserKeycloakIdAndId(userId, id)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND.getReasonPhrase(), "Store doesn't exist"));

        kaspiStoreRepository.save(mapToEntity(changeRequest, storeToDelete));
    }

    private KaspiStore mapToEntity(KaspiStoreChangeRequest changeRequest, KaspiStore kaspiStore){
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

    public List<StoreResponse.AvailableWorkTime> getAvailableTimesByStoreId(List<KaspiStoreAvailableTimes> availableTimes) {

        final var awts = new ArrayList<StoreResponse.AvailableWorkTime>();

        availableTimes.forEach(i -> {
            final var awt = StoreResponse.AvailableWorkTime.builder()
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
