package kz.wonder.wonderuserrepository.services;

import kz.wonder.wonderuserrepository.dto.request.SellerRegistrationRequest;
import kz.wonder.wonderuserrepository.dto.response.AuthResponse;
import org.keycloak.admin.client.resource.UserResource;

public interface KeycloakService {
    UserResource createUser(SellerRegistrationRequest sellerRegistrationRequest);
    AuthResponse getAuthResponse(String email, String password);

    void deleteUserById(String userId);

    UserResource getUserById(String id);

}
