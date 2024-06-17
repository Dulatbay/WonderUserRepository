package kz.wonder.wonderuserrepository.services;

import kz.wonder.wonderuserrepository.dto.request.PackageProductRequest;
import kz.wonder.wonderuserrepository.dto.response.StartPackageResponse;

public interface PackageService {
    StartPackageResponse startPackaging(String orderCode, String keycloakId);

    void packageProduct(String orderCode, PackageProductRequest packageProductRequest, String keycloakId);

    void finishPackaging(String orderCode, String keycloakId);
}
