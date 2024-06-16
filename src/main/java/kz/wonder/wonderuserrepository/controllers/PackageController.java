package kz.wonder.wonderuserrepository.controllers;

import jakarta.validation.Valid;
import kz.wonder.wonderuserrepository.constants.Utils;
import kz.wonder.wonderuserrepository.dto.request.PackageProductRequest;
import kz.wonder.wonderuserrepository.dto.response.OrderPackageDetailResponse;
import kz.wonder.wonderuserrepository.security.authorizations.base.StoreEmployeeAuthorization;
import kz.wonder.wonderuserrepository.services.PackageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RestController
@StoreEmployeeAuthorization
@RequestMapping("/order-packages")
public class PackageController {
    private final PackageService packageService;


    @PostMapping("/{orderCode}/start/")
    public ResponseEntity<OrderPackageDetailResponse> startPackageOrder(@PathVariable String orderCode) {
        var token = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var keycloakId = Utils.extractIdFromToken(token);

        packageService.startPackaging(orderCode, keycloakId);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/{orderCode}/finish")
    public ResponseEntity<Void> finishPackageOrder(@PathVariable String orderCode) {
        var token = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var keycloakId = Utils.extractIdFromToken(token);

        packageService.finishPackaging(orderCode, keycloakId);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping("/{orderCode}/package-product")
    public ResponseEntity<OrderPackageDetailResponse> packageProduct(@PathVariable String orderCode, @RequestBody @Valid PackageProductRequest packageProductRequest) {
        var token = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var keycloakId = Utils.extractIdFromToken(token);

        packageService.packageProduct(orderCode, packageProductRequest, keycloakId);

        return ResponseEntity.status(HttpStatus.OK).build();
    }


//    @GetMapping()
//    public ResponseEntity<List<OrderPackageResponse>> getAllPackages(@ModelAttribute PackageSearchParams searchParams) {
//        var token = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
//        var keycloakId = Utils.extractIdFromToken(token);
//
//        List<OrderPackageResponse> orderPackageResponses = packageService.getAllPackages(searchParams,keycloakId);
//
//        return ResponseEntity.ok(orderPackageResponses);
//    }
//
//    @GetMapping("/{orderCode}")
//    public ResponseEntity<OrderPackageDetailResponse> getPackageById(@PathVariable String orderCode) {
//        var token = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
//        var keycloakId = Utils.extractIdFromToken(token);
//
//        OrderPackageDetailResponse orderPackageResponses = packageService.getPackageByOrderId(orderCode, keycloakId);
//
//        return ResponseEntity.ok(orderPackageResponses);
//    }
}
