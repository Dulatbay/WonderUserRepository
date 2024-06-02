package kz.wonder.wonderuserrepository.controllers;

import kz.wonder.wonderuserrepository.dto.response.DailyStats;
import kz.wonder.wonderuserrepository.services.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/statistics")
public class StatisticsController {
    private final StatisticsService statisticsService;

    @GetMapping("/sales-information/admin-stats")
    public ResponseEntity<?> getAdminStatistics() {
        return ResponseEntity.ok().build();
    }

    @GetMapping("/sales-information/seller-stats")
    public ResponseEntity<?> getSellerStatistics() {
        return ResponseEntity.ok().build();
    }

    @GetMapping("/daily/seller-stats")
    public ResponseEntity<List<DailyStats>> getSellerDailyStats() {
        return ResponseEntity.ok().build();
    }

    @GetMapping("/daily/admin-stats")
    public ResponseEntity<List<DailyStats>> getAdminDailyStats() {
        return ResponseEntity.ok().build();
    }

    @GetMapping("/products-count/seller-stats")
    public ResponseEntity<?> getSellerProductsCount() {
        return ResponseEntity.ok().build();
    }

    @GetMapping("/last-orders/admin-stats")
    public ResponseEntity<?> getAdminLastOrders() {
        return ResponseEntity.ok().build();
    }

    @GetMapping("/top-products/seller-stats")
    public ResponseEntity<?> getSellerTopProducts() {
        return ResponseEntity.ok().build();
    }

    @GetMapping("/top-sellers/admin-stats")
    public ResponseEntity<?> getAdminTopSellers() {
        return ResponseEntity.ok().build();
    }
}
