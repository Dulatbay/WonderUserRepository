package kz.wonder.wonderuserrepository.services;

import kz.wonder.wonderuserrepository.dto.request.SupplyCreateRequest;
import kz.wonder.wonderuserrepository.dto.response.SupplyProcessFileResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface SupplyService {
    List<SupplyProcessFileResponse> processFile(MultipartFile file, String userId);

    void createSupply(SupplyCreateRequest createRequest, String userId);
}
