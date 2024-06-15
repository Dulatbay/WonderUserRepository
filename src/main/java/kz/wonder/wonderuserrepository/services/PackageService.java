package kz.wonder.wonderuserrepository.services;

import kz.wonder.wonderuserrepository.dto.params.PackageSearchParams;
import kz.wonder.wonderuserrepository.dto.response.OrderPackageDetailResponse;
import kz.wonder.wonderuserrepository.dto.response.OrderPackageResponse;

import java.util.List;

public interface PackageService {
    OrderPackageDetailResponse packageOrderByCode(String orderCode, String keycloakId);

    OrderPackageDetailResponse packageProduct(String orderCode, String productArticle, String keycloakId);

    List<OrderPackageResponse> getAllPackages(PackageSearchParams searchParams, String keycloakId);

    OrderPackageDetailResponse getPackageByOrderId(String orderId, String keycloakId);
}
