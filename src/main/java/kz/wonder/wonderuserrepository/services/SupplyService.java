package kz.wonder.wonderuserrepository.services;

import kz.wonder.wonderuserrepository.dto.request.SupplyCreateRequest;
import kz.wonder.wonderuserrepository.dto.response.SupplyAdminResponse;
import kz.wonder.wonderuserrepository.dto.response.SupplyProcessFileResponse;
import kz.wonder.wonderuserrepository.dto.response.SupplyProductResponse;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

public interface SupplyService {
    List<SupplyProcessFileResponse> processFile(MultipartFile file, String userId);

    void createSupply(SupplyCreateRequest createRequest, String userId);

	List<SupplyAdminResponse> getSuppliesOfAdmin(LocalDate startDate, LocalDate endDate, String userId, String username);

	List<SupplyProductResponse> getSuppliesDetail(Long id);
}
