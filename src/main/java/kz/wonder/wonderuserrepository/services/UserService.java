package kz.wonder.wonderuserrepository.services;

import jakarta.transaction.Transactional;
import kz.wonder.wonderuserrepository.dto.request.SellerRegistrationRequest;
import kz.wonder.wonderuserrepository.entities.WonderUser;

public interface UserService {
    // todo: remove to another service(SellerService)

    WonderUser getUserByKeycloakId(String keycloakId);
    WonderUser getUserById(Long id);

    @Transactional
    void syncUsersBetweenDBAndKeycloak();
}
