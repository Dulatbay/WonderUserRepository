package kz.wonder.wonderuserrepository.services;

import kz.wonder.wonderuserrepository.dto.request.BoxTypeCreateRequest;
import kz.wonder.wonderuserrepository.dto.response.BoxTypeResponse;

import java.util.List;

public interface BoxTypeService {
    void createBoxType(BoxTypeCreateRequest boxTypeCreateRequest);
    List<BoxTypeResponse> getAll();

    void deleteById(Long id);
}
