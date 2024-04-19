package kz.wonder.wonderuserrepository.services;

import kz.wonder.wonderuserrepository.dto.request.StoreCellCreateRequest;
import kz.wonder.wonderuserrepository.dto.response.StoreCellResponse;

import java.util.List;

public interface StoreCellService {
    void create(StoreCellCreateRequest storeCellCreateRequest, String keycloakId);

    List<StoreCellResponse> getAllByParams(Long storeId, String keycloakId);

    void delete(Long cellId, String keycloakId);
}
