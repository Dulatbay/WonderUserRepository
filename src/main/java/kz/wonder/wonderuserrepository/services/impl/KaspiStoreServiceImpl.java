package kz.wonder.wonderuserrepository.services.impl;


import jakarta.transaction.Transactional;
import kz.wonder.wonderuserrepository.dto.request.KaspiStoreCreateRequest;
import kz.wonder.wonderuserrepository.dto.response.StoreResponse;
import kz.wonder.wonderuserrepository.entities.KaspiCity;
import kz.wonder.wonderuserrepository.entities.KaspiStore;
import kz.wonder.wonderuserrepository.entities.KaspiStoreAvailableTimes;
import kz.wonder.wonderuserrepository.exceptions.DbObjectNotFoundException;
import kz.wonder.wonderuserrepository.repositories.KaspiCityRepository;
import kz.wonder.wonderuserrepository.repositories.KaspiStoreAvailableTimesRepository;
import kz.wonder.wonderuserrepository.repositories.KaspiStoreRepository;
import kz.wonder.wonderuserrepository.services.KaspiStoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static kz.wonder.wonderuserrepository.constants.ValueConstants.DATE_TIME_FORMATTER;


@Service
@RequiredArgsConstructor
public class KaspiStoreServiceImpl implements KaspiStoreService {

    private final KaspiStoreRepository kaspiStoreRepository;
    private final KaspiCityRepository kaspiCityRepository;
    private final KaspiStoreAvailableTimesRepository kaspiStoreAvailableTimesRepository;

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void createStore(final KaspiStoreCreateRequest kaspiStoreCreateRequest) {
        final KaspiStore kaspiStore = new KaspiStore();

        final var selectedCity = kaspiCityRepository.findById(kaspiStoreCreateRequest.getCityId())
                .orElseThrow(
                        () -> new DbObjectNotFoundException(HttpStatus.BAD_GATEWAY.getReasonPhrase(), "City doesn't exist")
                );

        final List<KaspiStoreAvailableTimes> availableTimes = new ArrayList<>();

        kaspiStoreCreateRequest
                .getDayOfWeekWorks().forEach(i -> {
                    try {
                        final var dayOfWeek = DayOfWeek.of(i.numericDayOfWeek());

                        final var closeTime = LocalTime.parse(i.closeTime(), DATE_TIME_FORMATTER);
                        final var openTime = LocalTime.parse(i.openTime(), DATE_TIME_FORMATTER);

                        final var workTime = new KaspiStoreAvailableTimes();

                        workTime.setDayOfWeek(dayOfWeek);
                        workTime.setOpenTime(openTime);
                        workTime.setCloseTime(closeTime);
                        workTime.setKaspiStore(kaspiStore);

                        availableTimes.add(workTime);

                    } catch (DateTimeParseException e) {
                        throw new IllegalArgumentException("Incorrect work time format");
                    }
                });

        kaspiStore.setUser(kaspiStore.getUser());
        kaspiStore.setKaspiCity(selectedCity);
        kaspiStore.setKaspiId(kaspiStoreCreateRequest.getKaspiId());
        kaspiStore.setName(kaspiStoreCreateRequest.getKaspiId());
        kaspiStore.setApartment(kaspiStoreCreateRequest.getApartment());
        kaspiStore.setStreet(kaspiStoreCreateRequest.getStreet());

        kaspiStoreRepository.save(kaspiStore);
        kaspiStoreAvailableTimesRepository.saveAll(availableTimes);
    }

    @Override
    public List<StoreResponse> getAllByUser(Long userId) {
        var kaspiStores = kaspiStoreRepository.findAllByUserId(userId);


        return kaspiStores.stream().map(i ->
                StoreResponse.builder()
                        .id(i.getId())
                        .kaspiId(i.getKaspiId())
                        .city(kaspiCityRepository.findByName(i.getName())
                                .orElse(new KaspiCity()).getName())
                        .street(i.getStreet())
                        .address(i.getApartment())
                        .availableWorkTimes(getAvailableTimesByStoreId(i.getId()))
                        .enabled(i.isEnabled())
                        .build()).collect(Collectors.toList());
    }

    public List<StoreResponse.AvailableWorkTime> getAvailableTimesByStoreId(Long kaspiStoreId) {
        var kaspiStoreAvailableTimes = kaspiStoreAvailableTimesRepository.findByKaspiStoreId(kaspiStoreId);

        var awts = new ArrayList<StoreResponse.AvailableWorkTime>();

        kaspiStoreAvailableTimes.forEach(i -> {
            var awt = StoreResponse.AvailableWorkTime.builder()
                    .id(i.getId())
                    .openTime(i.getOpenTime().format(DATE_TIME_FORMATTER).toLowerCase())
                    .closeTime(i.getCloseTime().format(DATE_TIME_FORMATTER).toLowerCase())
                    .dayOfWeek(i.getDayOfWeek().ordinal())
                    .build();

            awts.add(awt);
        });

        return awts;
    }
}
