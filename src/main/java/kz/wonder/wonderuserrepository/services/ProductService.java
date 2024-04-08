package kz.wonder.wonderuserrepository.services;

import jakarta.xml.bind.JAXBException;
import kz.wonder.wonderuserrepository.dto.response.ProductResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface ProductService {
    List<ProductResponse> processExcelFile(MultipartFile file, String token);

    List<ProductResponse> getProductsByKeycloakId(String keycloakUserId);

    String generateOfProductsXmlByKeycloakId(String userId) throws IOException, javax.xml.bind.JAXBException;

	void deleteProductById(String keycloakId, Long productId);
}
