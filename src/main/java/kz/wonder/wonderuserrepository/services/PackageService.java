package kz.wonder.wonderuserrepository.services;

import kz.wonder.wonderuserrepository.dto.response.StartPackageResponse;

public interface PackageService {
    StartPackageResponse startPackaging(String orderCode, String keycloakId);

    void packageProductStart(String orderCode, String productArticle, String keycloakId);

    void finishPackaging(String orderCode, String keycloakId);

    void packageProductFinish(String orderCode, String productArticle, String keycloakId);
}
