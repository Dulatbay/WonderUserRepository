package kz.wonder.wonderuserrepository.services;

import kz.wonder.wonderuserrepository.dto.request.SellerRegistrationRequest;
import kz.wonder.wonderuserrepository.dto.response.AuthResponse;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.UserRepresentation;

import java.util.List;

public interface KeycloakService {
    UserResource createUser(SellerRegistrationRequest sellerRegistrationRequest);
    AuthResponse getAuthResponse(String email, String password);

    void deleteUserById(String userId);

    List<UserRepresentation> getAllUsers();

    UserResource getUserById(String id);

}
