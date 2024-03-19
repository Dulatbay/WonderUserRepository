package kz.wonder.wonderuserrepository.services;


import kz.wonder.wonderuserrepository.dto.request.KaspiStoreChangeRequest;
import kz.wonder.wonderuserrepository.dto.request.KaspiStoreCreateRequest;
import kz.wonder.wonderuserrepository.dto.response.StoreResponse;

import java.util.List;

public interface KaspiStoreService {

    void createStore(KaspiStoreCreateRequest kaspiStoreCreateRequest);

    List<StoreResponse> getAllByUser(String keycloakUserId);

    List<StoreResponse> getAll();

    void deleteById(Long id, String keycloakUserId);

    void deleteById(Long id);

    void changeStore(KaspiStoreChangeRequest kaspiStoreCreateRequest, Long id);

    void changeStore(KaspiStoreChangeRequest changeRequest, Long id, String userId);
}
