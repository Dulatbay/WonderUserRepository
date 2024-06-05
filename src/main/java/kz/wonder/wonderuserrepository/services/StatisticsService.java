package kz.wonder.wonderuserrepository.services;

import kz.wonder.wonderuserrepository.dto.params.DurationParams;
import kz.wonder.wonderuserrepository.dto.response.AdminSalesInformation;
import kz.wonder.wonderuserrepository.dto.response.ProductWithCount;
import kz.wonder.wonderuserrepository.dto.response.SellerSalesInformation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface StatisticsService {
    AdminSalesInformation getAdminSalesInformation(String keycloakId, DurationParams duration);

    SellerSalesInformation getSellerSalesInformation(String keycloakId, DurationParams duration);

    Page<ProductWithCount> getSellerProductsCountInformation(String keycloakId,  Pageable pageable);
}
