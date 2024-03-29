package kz.wonder.wonderuserrepository.services;

import jakarta.transaction.Transactional;
import kz.wonder.wonderuserrepository.dto.request.SellerRegistrationRequest;
import kz.wonder.wonderuserrepository.entities.WonderUser;

public interface UserService {
    void createUser(SellerRegistrationRequest sellerRegistrationRequest);
    WonderUser getUserByKeycloakId(String keycloakId);

    @Transactional
    void syncUsersBetweenDBAndKeycloak();
}
