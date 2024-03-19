package kz.wonder.wonderuserrepository.services;


import kz.wonder.wonderuserrepository.dto.request.KaspiStoreCreateRequest;
import kz.wonder.wonderuserrepository.dto.response.StoreResponse;

import java.util.List;

public interface KaspiStoreService {

    void createStore(KaspiStoreCreateRequest kaspiStoreCreateRequest);

    List<StoreResponse> getAllByUser(String keycloakUserId);
}
