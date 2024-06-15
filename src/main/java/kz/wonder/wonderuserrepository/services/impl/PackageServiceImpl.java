package kz.wonder.wonderuserrepository.services.impl;

import kz.wonder.wonderuserrepository.dto.params.PackageSearchParams;
import kz.wonder.wonderuserrepository.dto.response.OrderPackageDetailResponse;
import kz.wonder.wonderuserrepository.dto.response.OrderPackageResponse;
import kz.wonder.wonderuserrepository.repositories.*;
import kz.wonder.wonderuserrepository.services.PackageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PackageServiceImpl implements PackageService {
    private final PackageOrderProcessRepository packageOrderProcessRepository;
    private final PackageOrderRepository packageOrderRepository;
    private final OrderAssembleRepository orderAssembleRepository;
    private final StoreEmployeeRepository storeEmployeeRepository;
    private final SupplyBoxProductsRepository supplyBoxProductsRepository;


    @Override
    public OrderPackageDetailResponse packageOrderByCode(String orderCode, String keycloakId) {
        return null;
    }

    @Override
    public OrderPackageDetailResponse packageProduct(String orderCode, String productArticle, String keycloakId) {
        return null;
    }

    @Override
    public List<OrderPackageResponse> getAllPackages(PackageSearchParams searchParams, String keycloakId) {
        return List.of();
    }

    @Override
    public OrderPackageDetailResponse getPackageByOrderId(String orderId, String keycloakId) {
        return null;
    }
}
