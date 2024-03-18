package kz.wonder.wonderuserrepository.services;

import kz.wonder.wonderuserrepository.dto.request.SellerRegistrationRequest;
import kz.wonder.wonderuserrepository.entities.User;

public interface UserService {
    void createUser(SellerRegistrationRequest sellerRegistrationRequest);
    User getUserByKeycloakId(String keycloakId);
}
