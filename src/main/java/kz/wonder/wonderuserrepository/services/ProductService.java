package kz.wonder.wonderuserrepository.services;

import kz.wonder.wonderuserrepository.dto.response.ProductResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ProductService {
    List<ProductResponse> processExcelFile(MultipartFile file, String token);

    List<ProductResponse> getProductsByKeycloakId(String keycloakUserId);
}
