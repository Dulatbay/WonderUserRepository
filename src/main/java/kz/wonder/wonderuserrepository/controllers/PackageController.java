package kz.wonder.wonderuserrepository.controllers;

import jakarta.validation.Valid;
import kz.wonder.wonderuserrepository.constants.Utils;
import kz.wonder.wonderuserrepository.dto.response.StartPackageResponse;
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


    @PostMapping("/{orderCode}/start")
    public ResponseEntity<StartPackageResponse> startPackageOrder(@PathVariable String orderCode) {
        var token = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var keycloakId = Utils.extractIdFromToken(token);

        StartPackageResponse startPackageResponse =  packageService.startPackaging(orderCode, keycloakId);

        return ResponseEntity.status(HttpStatus.CREATED).body(startPackageResponse);
    }

    @PostMapping("/{orderCode}/finish")
    public ResponseEntity<Void> finishPackageOrder(@PathVariable String orderCode) {
        var token = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var keycloakId = Utils.extractIdFromToken(token);

        packageService.finishPackaging(orderCode, keycloakId);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping("/{orderCode}/package-product/start")
    public ResponseEntity<Void> packageProductStart(@PathVariable String orderCode, @RequestParam("product-article") String productArticle) {
        var token = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var keycloakId = Utils.extractIdFromToken(token);

        packageService.packageProductStart(orderCode, productArticle, keycloakId);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PostMapping("/{orderCode}/package-product/finish")
    public ResponseEntity<Void> packageProductEnd(@PathVariable String orderCode, @RequestParam("product-article") String productArticle) {
        var token = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var keycloakId = Utils.extractIdFromToken(token);

        packageService.packageProductFinish(orderCode, productArticle, keycloakId);

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
