package kz.wonder.wonderuserrepository.services;

import kz.wonder.wonderuserrepository.dto.request.SupplyCreateRequest;
import kz.wonder.wonderuserrepository.dto.request.SupplyScanRequest;
import kz.wonder.wonderuserrepository.dto.response.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

public interface SupplyService {
    List<SupplyProcessFileResponse> processFile(MultipartFile file, String userId);

    SupplySellerResponse createSupply(SupplyCreateRequest createRequest, String userId);

    void rejectSupplyById(Long supplyId);

    List<SupplyAdminResponse> getSuppliesOfAdmin(LocalDate startDate, LocalDate endDate, String userId, String username, String id);

    List<SupplyProductResponse> getSuppliesDetail(Long id);

    List<SupplyProductResponse> getSuppliesDetail(Long id, String keycloakId);

    List<SupplyProductResponse> getSuppliesDetailOfSeller(Long id, String keycloakId);

    List<SupplySellerResponse> getSuppliesOfSeller(String id, LocalDate startDate, LocalDate endDate);

    List<SupplyStateResponse> getSupplySellerState(Long supplyId, String keycloakId);

    List<SupplyStorageResponse> getSuppliesOfStorage(Long employeeId, LocalDate startDate, LocalDate endDate);

    List<SupplyStorageResponse> getSuppliesOfStorage(String keycloakId, LocalDate startDate, LocalDate endDate);

    ProductStorageResponse getSuppliesProducts(String keycloakId, Long supplyId);

    ProductStorageResponse getSuppliesProducts(String keycloakId, String boxVendorCode, boolean isSuperAdmin);

    void processSupplyByEmployee(String keycloakId, SupplyScanRequest supplyScanRequest);

    SellerSupplyReport getSupplySellerReport(Long supplyId, String keycloakId);

    void uploadAuthorityDocument(MultipartFile file, Long supplyId, String keycloakId);
}
