package kz.wonder.wonderuserrepository.services;

import kz.wonder.wonderuserrepository.dto.params.DurationParams;
import kz.wonder.wonderuserrepository.dto.response.AdminSalesInformation;
import kz.wonder.wonderuserrepository.dto.response.SellerSalesInformation;

public interface StatisticsService {
    AdminSalesInformation getAdminSalesInformation(String keycloakId, DurationParams duration);

    SellerSalesInformation getSellerSalesInformation(String keycloakId, DurationParams duration);
}
