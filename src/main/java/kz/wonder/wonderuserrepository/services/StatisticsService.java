package kz.wonder.wonderuserrepository.services;

import kz.wonder.wonderuserrepository.dto.params.DurationParams;
import kz.wonder.wonderuserrepository.dto.response.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface StatisticsService {
    AdminSalesInformation getAdminSalesInformation(String keycloakId, DurationParams duration);

    SellerSalesInformation getSellerSalesInformation(String keycloakId, DurationParams duration);

    Page<ProductWithCount> getSellerProductsCountInformation(String keycloakId, Pageable pageable);

    Page<AdminLastOrdersInformation> getAdminLastOrders(String keycloakId, Pageable pageable);

    Page<SellerTopProductInformation> getSellerTopProductsInformation(String keycloakId, DurationParams durationParams, Pageable pageable);

    Page<AdminTopSellerInformation> getAdminTopSellersInformation(String keycloakId, DurationParams durationParams, Pageable pageable);

    List<DailyStats> getSellerDailyStats(String keycloakId, DurationParams durationParams);

    List<DailyStats> getAdminDailyStats(String keycloakId, DurationParams durationParams);
}
