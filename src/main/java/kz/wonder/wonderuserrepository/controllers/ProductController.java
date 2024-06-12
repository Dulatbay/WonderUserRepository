package kz.wonder.wonderuserrepository.controllers;

import kz.wonder.wonderuserrepository.constants.Utils;
import kz.wonder.wonderuserrepository.dto.base.PaginatedResponse;
import kz.wonder.wonderuserrepository.dto.request.ProductPriceChangeRequest;
import kz.wonder.wonderuserrepository.dto.params.ProductSearchParams;
import kz.wonder.wonderuserrepository.dto.request.ProductSizeChangeRequest;
import kz.wonder.wonderuserrepository.dto.response.*;
import kz.wonder.wonderuserrepository.security.keycloak.KeycloakRole;
import kz.wonder.wonderuserrepository.services.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/products")
public class ProductController {
    private final ProductService productService;

    @PostMapping(name = "/by-file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<ProductResponse>> createByFile(@RequestPart("file") MultipartFile file) {
        var token = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var userId = Utils.extractIdFromToken(token);
        productService.processExcelFile(file, userId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .build();
    }

    @GetMapping()
    public ResponseEntity<PaginatedResponse<ProductResponse>> getProducts(@RequestParam(defaultValue = "0") int page,
                                                                          @RequestParam(defaultValue = "10") int size,
                                                                          @RequestParam(name = "searchValue", required = false) String searchValue,
                                                                          @RequestParam(name = "isPublished", required = false) Boolean isPublished,
                                                                          @RequestParam(name = "sortBy", defaultValue = "id") String sortBy) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
        var token = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        Page<ProductResponse> productPage = productService.findAllByKeycloakId(Utils.extractIdFromToken(token), pageable, isPublished, searchValue);
        PaginatedResponse<ProductResponse> response = new PaginatedResponse<>(productPage);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/prices")
    public ResponseEntity<PaginatedResponse<ProductPriceResponse>> getProductPrices(@RequestParam(defaultValue = "0") int page,
                                                                                    @RequestParam(defaultValue = "10") int size,
                                                                                    @RequestParam(name = "searchValue", required = false) String searchValue,
                                                                                    @RequestParam(name = "isPublished", required = false) Boolean isPublished,
                                                                                    @RequestParam(name = "sortBy", defaultValue = "id") String sortBy) {
        var token = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var keycloakId = Utils.extractIdFromToken(token);
        var isSuperAdmin = Utils.getAuthorities(token.getAuthorities()).contains(KeycloakRole.SUPER_ADMIN.name());

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));


        var productsPrices = productService.getProductsPrices(keycloakId, isSuperAdmin, pageable, isPublished, searchValue);
        var response = new PaginatedResponse<>(productsPrices);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/publish")
    public ResponseEntity<Void> updatePublish(@RequestParam Long productId, @RequestParam Boolean isPublished) {
        var token = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var keycloakId = Utils.extractIdFromToken(token);

        productService.changePublish(keycloakId, productId, isPublished);

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/price")
    public ResponseEntity<Void> updatePrice(@RequestBody ProductPriceChangeRequest productPriceChangeRequest) {
        var token = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var keycloakId = Utils.extractIdFromToken(token);

        productService.changePrice(keycloakId, productPriceChangeRequest);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> getProduct(@PathVariable Long productId) {
        var token = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        // todo: make also for superAdmin
        var keycloakId = Utils.extractIdFromToken(token);

        productService.deleteProductById(keycloakId, productId);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/xml")
    public ResponseEntity<String> getXmlOfProducts() {
        var token = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var userId = Utils.extractIdFromToken(token);

        String pathToXml;
        try {
            pathToXml = productService.generateOfProductsXmlByKeycloakId(userId);
        } catch (IOException e) {
            log.error("IOException: ", e);
            throw new RuntimeException(e);
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(pathToXml);
    }

    @GetMapping("/search-by-params")
    public ResponseEntity<PaginatedResponse<ProductSearchResponse>> searchProducts(@ModelAttribute ProductSearchParams productSearchParams,
                                                                                   @RequestParam(defaultValue = "0") int page,
                                                                                   @RequestParam(defaultValue = "10") int size) {
        var token = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var keycloakId = Utils.extractIdFromToken(token);

        PageRequest pageRequest = PageRequest.of(page, size);


        Page<ProductSearchResponse> response = productService.searchByParams(productSearchParams, pageRequest, keycloakId);

        return ResponseEntity.ok(new PaginatedResponse<>(response));
    }

    @PatchMapping("/change-size/{originVendorCode}")
    public ResponseEntity<Void> changeSize(@PathVariable String originVendorCode, @RequestBody ProductSizeChangeRequest productSizeChangeRequest) {
        var token = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var keycloakId = Utils.extractIdFromToken(token);

        productService.changeSize(originVendorCode, productSizeChangeRequest, keycloakId);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/get-with-sizes")
    public ResponseEntity<PaginatedResponse<ProductWithSize>> getWithSizes(
            @ModelAttribute ProductSearchParams productSearchParams,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        var token = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var keycloakId = Utils.extractIdFromToken(token);

        PageRequest pageRequest = PageRequest.of(page, size);

        Page<ProductWithSize> response = productService.getProductsSizes(productSearchParams, keycloakId, pageRequest);

        return ResponseEntity.ok(new PaginatedResponse<>(response));
    }


}
