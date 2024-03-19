package kz.wonder.wonderuserrepository.services;

import org.springframework.web.multipart.MultipartFile;

public interface SupplyService {
    void createSupply(MultipartFile file, String userId, Long storeId);
}
