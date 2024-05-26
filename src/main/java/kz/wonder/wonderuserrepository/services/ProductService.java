package kz.wonder.wonderuserrepository.services;

import kz.wonder.wonderuserrepository.dto.request.ProductPriceChangeRequest;
import kz.wonder.wonderuserrepository.dto.response.ProductPriceResponse;
import kz.wonder.wonderuserrepository.dto.response.ProductResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import org.springframework.data.domain.Page;

public interface ProductService {
    void processExcelFile(MultipartFile file, String token);

    Page<ProductResponse> findAllByKeycloakId(String keycloakUserId, Pageable pageable, Boolean isPublished, String searchValue);

    String generateOfProductsXmlByKeycloakId(String userId) throws IOException, javax.xml.bind.JAXBException;

    void deleteProductById(String keycloakId, Long productId);

    Page<ProductPriceResponse> getProductsPrices(String keycloakId, boolean isSuperAdmin, Pageable pageable, Boolean isPublished, String searchValue);

    void changePublish(String keycloakId, Long productId, Boolean isPublished);

    void changePrice(String keycloakId, ProductPriceChangeRequest productPriceChangeRequest);
}
