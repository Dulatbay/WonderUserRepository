package kz.wonder.wonderuserrepository.controllers;

import kz.wonder.wonderuserrepository.constants.Utils;
import kz.wonder.wonderuserrepository.dto.base.PaginatedResponse;
import kz.wonder.wonderuserrepository.dto.params.DurationParams;
import kz.wonder.wonderuserrepository.dto.response.*;
import kz.wonder.wonderuserrepository.security.authorizations.AccessForAdmins;
import kz.wonder.wonderuserrepository.security.authorizations.base.SellerAuthorization;
import kz.wonder.wonderuserrepository.services.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/statistics")
public class StatisticsController {
    private final StatisticsService statisticsService;

    @GetMapping("/sales-information/admin-stats")
    @AccessForAdmins
    public ResponseEntity<AdminSalesInformation> getAdminStatistics(@RequestParam("duration") DurationParams duration) {
        var token = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var keycloakId = Utils.extractIdFromToken(token);

        AdminSalesInformation adminSalesInformation = statisticsService.getAdminSalesInformation(keycloakId, duration);

        return ResponseEntity.ok(adminSalesInformation);
    }

    @GetMapping("/sales-information/seller-stats")
    @SellerAuthorization
    public ResponseEntity<SellerSalesInformation> getSellerStatistics(@RequestParam("duration") DurationParams duration) {
        var token = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var keycloakId = Utils.extractIdFromToken(token);

        SellerSalesInformation sellerSalesInformation = statisticsService.getSellerSalesInformation(keycloakId, duration);

        return ResponseEntity.ok(sellerSalesInformation);
    }

    @GetMapping("/daily/seller-stats")
    @SellerAuthorization
    public ResponseEntity<List<DailyStats>> getSellerDailyStats(@RequestParam("duration") DurationParams durationParams) {
        var token = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var keycloakId = Utils.extractIdFromToken(token);

        List<DailyStats> dailyStats = statisticsService.getSellerDailyStats(keycloakId, durationParams);

        return ResponseEntity.ok(dailyStats);
    }

    @GetMapping("/daily/admin-stats")
    @AccessForAdmins
    public ResponseEntity<List<DailyStats>> getAdminDailyStats(@RequestParam("duration") DurationParams durationParams) {
        var token = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var keycloakId = Utils.extractIdFromToken(token);

        List<DailyStats> dailyStats = statisticsService.getAdminDailyStats(keycloakId, durationParams);

        return ResponseEntity.ok(dailyStats);
    }

    @GetMapping("/products-count/seller-stats")
    @SellerAuthorization
    public ResponseEntity<PaginatedResponse<ProductWithCount>> getSellerProductsCount(@RequestParam(defaultValue = "0") int page,
                                                                                      @RequestParam(defaultValue = "10") int size) {
        var token = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var keycloakId = Utils.extractIdFromToken(token);

        Pageable pageable = PageRequest.of(page, size);

        Page<ProductWithCount> products = statisticsService.getSellerProductsCountInformation(keycloakId, pageable);


        return ResponseEntity.ok(new PaginatedResponse<>(products));
    }

    @GetMapping("/last-orders/admin-stats")
    @AccessForAdmins
    public ResponseEntity<PaginatedResponse<AdminLastOrdersInformation>> getAdminLastOrders(@RequestParam(defaultValue = "0") int page,
                                                                                            @RequestParam(defaultValue = "10") int size) {
        var token = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var keycloakId = Utils.extractIdFromToken(token);

        Pageable pageable = PageRequest.of(page, size);

        Page<AdminLastOrdersInformation> ordersInformation = statisticsService.getAdminLastOrders(keycloakId, pageable);

        return ResponseEntity.ok(new PaginatedResponse<>(ordersInformation));
    }

    @GetMapping("/top-products/seller-stats")
    @SellerAuthorization
    public ResponseEntity<PaginatedResponse<SellerTopProductInformation>> getSellerTopProducts(@RequestParam(defaultValue = "0") int page,
                                                                                               @RequestParam(defaultValue = "10") int size,
                                                                                               @RequestParam(value = "duration", defaultValue = "MONTH") DurationParams durationParams) {
        var token = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var keycloakId = Utils.extractIdFromToken(token);

        Pageable pageable = PageRequest.of(page, size);

        Page<SellerTopProductInformation> sellerTopProductInformation = statisticsService.getSellerTopProductsInformation(keycloakId, durationParams, pageable);

        return ResponseEntity.ok(new PaginatedResponse<>(sellerTopProductInformation));
    }

    @GetMapping("/top-sellers/admin-stats")
    @AccessForAdmins
    public ResponseEntity<PaginatedResponse<AdminTopSellerInformation>> getAdminTopSellers(@RequestParam(defaultValue = "0") int page,
                                                                                           @RequestParam(defaultValue = "10") int size,
                                                                                           @RequestParam(name = "duration", defaultValue = "MONTH") DurationParams durationParams) {
        var token = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var keycloakId = Utils.extractIdFromToken(token);

        Pageable pageable = PageRequest.of(page, size);

        Page<AdminTopSellerInformation> adminTopSellerInformation = statisticsService.getAdminTopSellersInformation(keycloakId, durationParams, pageable);

        return ResponseEntity.ok(new PaginatedResponse<>(adminTopSellerInformation));
    }
}
