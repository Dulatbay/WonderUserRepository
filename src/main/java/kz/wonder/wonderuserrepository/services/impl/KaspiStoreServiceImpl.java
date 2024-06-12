package kz.wonder.wonderuserrepository.services.impl;


import jakarta.transaction.Transactional;
import kz.wonder.wonderuserrepository.dto.request.KaspiStoreChangeRequest;
import kz.wonder.wonderuserrepository.dto.request.KaspiStoreCreateRequest;
import kz.wonder.wonderuserrepository.dto.response.StoreDetailResponse;
import kz.wonder.wonderuserrepository.dto.response.StoreResponse;
import kz.wonder.wonderuserrepository.entities.KaspiCity;
import kz.wonder.wonderuserrepository.entities.KaspiStore;
import kz.wonder.wonderuserrepository.entities.KaspiStoreAvailableBoxTypes;
import kz.wonder.wonderuserrepository.entities.KaspiStoreAvailableTimes;
import kz.wonder.wonderuserrepository.exceptions.DbObjectNotFoundException;
import kz.wonder.wonderuserrepository.mappers.KaspiStoreMapper;
import kz.wonder.wonderuserrepository.repositories.BoxTypeRepository;
import kz.wonder.wonderuserrepository.repositories.KaspiCityRepository;
import kz.wonder.wonderuserrepository.repositories.KaspiStoreAvailableTimesRepository;
import kz.wonder.wonderuserrepository.repositories.KaspiStoreRepository;
import kz.wonder.wonderuserrepository.services.KaspiStoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;


@Slf4j
@Service
@RequiredArgsConstructor
public class KaspiStoreServiceImpl implements KaspiStoreService {
    private final KaspiStoreRepository kaspiStoreRepository;
    private final KaspiCityRepository kaspiCityRepository;
    private final KaspiStoreAvailableTimesRepository kaspiStoreAvailableTimesRepository;
    private final BoxTypeRepository boxTypeRepository;
    private final KaspiStoreMapper kaspiStoreMapper;

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void createStore(final KaspiStoreCreateRequest kaspiStoreCreateRequest) {
        KaspiStore kaspiStore = new KaspiStore();

        log.info("kaspiStoreCreateRequest.getWonderUser().getKeycloakId(): {}", kaspiStoreCreateRequest.getWonderUser().getKeycloakId());

        final var selectedCity = kaspiCityRepository.findById(kaspiStoreCreateRequest.getCityId())
                .orElseThrow(
                        () -> new DbObjectNotFoundException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase(), "Город не существует")
                );

        final List<KaspiStoreAvailableTimes> availableTimes = kaspiStoreMapper.mapToEntity(kaspiStoreCreateRequest.getDayOfWeekWorks(), kaspiStore);

        log.info("Available times with size: {}", availableTimes.size());

        String formattedAddress = KaspiStoreMapper.getFormattedAddress(kaspiStore, selectedCity);

        kaspiStore = kaspiStoreMapper.mapToCreateStore(kaspiStoreCreateRequest, selectedCity, formattedAddress);

        log.info("Created Kaspi store with id: {}", kaspiStore.getId());

        kaspiStoreRepository.save(kaspiStore);
        kaspiStoreAvailableTimesRepository.saveAll(availableTimes);
    }



    @Override
    public List<StoreResponse> getAllByUser(String keycloakUserId) {
        final var kaspiStores = kaspiStoreRepository.findAllByWonderUserKeycloakId(keycloakUserId);

        log.info("Retrieving kaspi stores with size: {}", kaspiStores.size());

        return kaspiStoreMapper.mapToResponses(kaspiStores);
    }

    @Override
    public List<StoreResponse> getAll() {
        final var kaspiStores = kaspiStoreRepository.findAll();
        return kaspiStoreMapper.mapToResponses(kaspiStores);
    }

    @Override
    public void deleteById(Long id, String keycloakUserId) {
        final var kaspiStore = kaspiStoreRepository.findByWonderUserKeycloakIdAndId(keycloakUserId, id)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND, HttpStatus.NOT_FOUND.getReasonPhrase(), "Kaspi магазин не существует"));

        kaspiStoreRepository.delete(kaspiStore);
        log.info("Store with id {} and keycloak user id {} were deleted", id, keycloakUserId);
    }

    @Override
    public void deleteById(Long id) {
        final var kaspiStore = kaspiStoreRepository.findById(id)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND, HttpStatus.NOT_FOUND.getReasonPhrase(), "Магазин не существует"));

        log.info("Store with id {} was deleted", id);
        kaspiStoreRepository.delete(kaspiStore);
    }

    @Override
    public void changeStore(KaspiStoreChangeRequest changeRequest, Long id) {
        final var kaspiStore = kaspiStoreRepository.findById(id)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND, HttpStatus.NOT_FOUND.getReasonPhrase(), "Магазин не существует"));

        log.info("Kaspi store with id: {}, was updated", kaspiStore.getId());

        var toSave = mapToEntity(changeRequest, kaspiStore);
        toSave.setFormattedAddress(KaspiStoreMapper.getFormattedAddress(toSave, kaspiStore.getKaspiCity()));

        kaspiStoreRepository.save(toSave);
    }

    @Override
    public void changeStore(KaspiStoreChangeRequest changeRequest, Long id, String userId) {
        final var kaspiStore = kaspiStoreRepository.findByWonderUserKeycloakIdAndId(userId, id)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND, HttpStatus.NOT_FOUND.getReasonPhrase(), "Магазин не существует"));

        var toSave = mapToEntity(changeRequest, kaspiStore);
        toSave.setFormattedAddress(KaspiStoreMapper.getFormattedAddress(toSave, kaspiStore.getKaspiCity()));

        kaspiStoreRepository.save(toSave);
    }

    @Override
    public void addBoxTypeToStore(Long boxTypeId, Long storeId) {
        var kaspiStore = kaspiStoreRepository.findById(storeId)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase(), "Kaspi магазин не существует"));

        addBoxTypeToStoreWithValidating(boxTypeId, kaspiStore);
    }

    @Override
    public void addBoxTypeToStoreWithValidating(Long boxTypeId, Long storeId, String keycloakUserId) {
        var kaspiStore = kaspiStoreRepository.findByWonderUserKeycloakIdAndId(keycloakUserId, storeId)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase(), "Kaspi магазин не существует"));

        addBoxTypeToStoreWithValidating(boxTypeId, kaspiStore);
    }

    private void addBoxTypeToStoreWithValidating(Long boxTypeId, KaspiStore kaspiStore) {
        var boxType = boxTypeRepository.findById(boxTypeId)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase(), "Тип коробки не существует"));

        var availableBoxType = new KaspiStoreAvailableBoxTypes();

        availableBoxType.setBoxType(boxType);
        availableBoxType.setKaspiStore(kaspiStore);
        availableBoxType.setEnabled(true);

        log.info("Available box type with id: {}", availableBoxType.getId());

        kaspiStore.getAvailableBoxTypes().add(availableBoxType);

        kaspiStoreRepository.save(kaspiStore);
    }

    @Override
    public List<StoreDetailResponse> getAllDetail() {
        return kaspiStoreMapper.mapToDetailResponse(kaspiStoreRepository.findAll());
    }

    @Override
    public List<StoreDetailResponse> getAllDetailByUser(String keycloakId) {
        return kaspiStoreMapper.mapToDetailResponse(kaspiStoreRepository.findAllByWonderUserKeycloakId(keycloakId));
    }

    @Override
    public void removeBoxType(Long boxTypeId, Long storeId) {
        var store = kaspiStoreRepository.findById(storeId)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase(), "Магазин не существует"));

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
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase(), "Магазин не существует"));

        store.getAvailableBoxTypes()
                .removeIf(i -> Objects.equals(i.getId(), storeId));

        kaspiStoreRepository.save(store);
    }

    @Override
    public StoreResponse getById(Long id, boolean isSuperAdmin, String keycloakId) {
        var store = kaspiStoreRepository.findById(id)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND,
                        HttpStatus.NOT_FOUND.getReasonPhrase(),
                        "Store doesn't exist"));

        var isHisStore = store
                .getWonderUser()
                .getKeycloakId()
                .equals(keycloakId);

        if (!isSuperAdmin && (!isHisStore && !store.isEnabled()))
            throw new IllegalStateException("Магазин не существует");

        return kaspiStoreMapper.mapToResponse(store);
    }


    @Override
    public StoreDetailResponse getByIdAndByUserDetail(Long storeId, boolean isSuperAdmin, String keycloakId) {
        var store = kaspiStoreRepository.findById(storeId)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.BAD_REQUEST,
                        HttpStatus.BAD_REQUEST.getReasonPhrase(),
                        "Магазин не существует"));

        var isHisStore = store.getWonderUser().getKeycloakId().equals(keycloakId);

        if (!isSuperAdmin && (!isHisStore && !store.isEnabled()))
            throw new IllegalArgumentException("Магазин не существует");

        log.info("Retrieving store with id: {}", store.getId());

        return kaspiStoreMapper.mapToDetailResponse(store);
    }


    @Override
    public List<StoreResponse> getAllForSeller() {
        // todo: в дальнейшем будем возвращать склады на основе его локации и тд
        return kaspiStoreMapper.mapToResponses(kaspiStoreRepository.findAllByEnabledIsTrue());
    }

    @Override
    public List<StoreDetailResponse> getAllDetailForSeller() {
        return kaspiStoreMapper.mapToDetailResponse(kaspiStoreRepository.findAllByEnabledIsTrue());
    }

    private KaspiStore mapToEntity(KaspiStoreChangeRequest changeRequest, KaspiStore kaspiStore) {
        var kaspiCity = kaspiCityRepository.findById(changeRequest.getCityId())
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase(), "City doesn't exist"));

        kaspiStore.setKaspiId(changeRequest.getKaspiId());
        kaspiStore.setEnabled(changeRequest.isEnabled());
        kaspiStore.setStreetName(changeRequest.getStreetName());
        kaspiStore.setStreetNumber(changeRequest.getStreetNumber());
        kaspiStore.setFormattedAddress(KaspiStoreMapper.getFormattedAddress(kaspiStore, kaspiCity));

        kaspiStore.setKaspiCity(kaspiCity);
        kaspiStore.getAvailableTimes().clear();
        kaspiStore.getAvailableTimes().addAll(kaspiStoreMapper.mapToEntity(changeRequest.getDayOfWeekWorks(), kaspiStore));
        return kaspiStore;
    }

}
