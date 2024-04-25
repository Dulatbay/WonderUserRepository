package kz.wonder.wonderuserrepository.services;


import kz.wonder.wonderuserrepository.dto.request.KaspiStoreChangeRequest;
import kz.wonder.wonderuserrepository.dto.request.KaspiStoreCreateRequest;
import kz.wonder.wonderuserrepository.dto.response.StoreDetailResponse;
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

    void addBoxTypeToStore(Long boxTypeId, Long storeId);

    void addBoxTypeToStore(Long boxTypeId, Long storeId, String keycloakUserId);

    List<StoreDetailResponse> getAllDetail();

    List<StoreDetailResponse> getAllDetailByUser(String keycloakId);

    void removeBoxType(Long boxTypeId, Long storeId);

    void removeBoxType(Long boxTypeId, Long storeId, String keycloakId);

	StoreResponse getById(Long id, boolean isSuperAdmin, String keycloakId);

    StoreDetailResponse getByIdAndByUserDetail(Long storeId, boolean isSuperAdmin, String keycloakId);

    List<StoreResponse> getAllForSeller();

    List<StoreDetailResponse> getAllDetailForSeller();
}
