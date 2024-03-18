package kz.wonder.wonderuserrepository.services.impl;

import kz.wonder.wonderuserrepository.dto.request.SellerRegistrationRequest;
import kz.wonder.wonderuserrepository.dto.response.AuthResponse;
import kz.wonder.wonderuserrepository.security.KeycloakRole;
import kz.wonder.wonderuserrepository.security.keycloak.KeycloakError;
import kz.wonder.wonderuserrepository.services.KeycloakService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.jboss.resteasy.client.jaxrs.internal.ResteasyClientBuilderImpl;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.core.Response;
import java.util.Collections;

@Service
@Slf4j
@RequiredArgsConstructor
public class KeycloakServiceImpl implements KeycloakService {

    @Value("${application.realm}")
    private String realm;

    @Value("${application.client-id}")
    private String clintId;

    @Value("${application.keycloak-url}")
    private String keycloakUrl;

    private final Keycloak keycloak;

    @Override
    public UserResource createUser(SellerRegistrationRequest sellerRegistrationRequest) {
        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setEnabled(true);
        userRepresentation.setFirstName(sellerRegistrationRequest.getFirstName());
        userRepresentation.setLastName(sellerRegistrationRequest.getLastName());
        userRepresentation.setEmail(sellerRegistrationRequest.getEmail());
        userRepresentation.setUsername(sellerRegistrationRequest.getEmail());
        String userId = null;
        try (Response response = getUsersResource().create(userRepresentation)) {

            if (response.getStatus() != 201) {
                log.info("response status: {}", response.getStatus());
                log.info("response entity: {}", response.getEntity());
                if (response.getStatus() == HttpStatus.SC_CONFLICT) {
                    var object = response.readEntity(KeycloakError.class);
                    throw new IllegalArgumentException(object.getErrorMessage());
                } else {
                    throw new InternalServerErrorException("Unknown error");
                }
            }

            userId = CreatedResponseUtil.getCreatedId(response);
            UserResource userResource = getUsersResource().get(userId);
            userResource.resetPassword(getPasswordCredential(sellerRegistrationRequest.getPassword(), false));
            userResource.roles() //
                    .clientLevel(getClient().getId())
                    .add(Collections.singletonList(getClientRole(getClient(), KeycloakRole.SELLER)));

            try {
                sendEmail(userId);
            } catch (Exception e) {
                throw new IllegalArgumentException("Email is incorrect");
            }


            return userResource;
        } catch (Exception e) {
//            if (userId != null) {
//                try (var response = getUsersResource().delete(userId)) {
//                    if(response.getStatus() ==
//                }
//            }
            if (userId != null)
                getUsersResource().delete(userId);
            throw e;
        }
    }

    private CredentialRepresentation getPasswordCredential(String password, boolean temporary) {
        CredentialRepresentation passwordCred = new CredentialRepresentation();
        passwordCred.setTemporary(temporary);
        passwordCred.setType(CredentialRepresentation.PASSWORD);
        passwordCred.setValue(password);
        return passwordCred;
    }

    private RoleRepresentation getClientRole(ClientRepresentation client, KeycloakRole keycloakRole) {
        return getRealmResource().clients().get(client.getId())
                .roles().get(keycloakRole.name()).toRepresentation();
    }

    private RealmResource getRealmResource() {
        return keycloak.realm(realm);
    }

    private UsersResource getUsersResource() {
        return getRealmResource().users();
    }

    private ClientRepresentation getClient() {
        return getRealmResource().clients()
                .findByClientId(clintId).getFirst();
    }

    private void sendEmail(String userId) {
        getUsersResource().get(userId).sendVerifyEmail();
    }

    private Keycloak getKeycloak(String username, String password){
        return KeycloakBuilder.builder()
                .serverUrl(keycloakUrl)
                .realm(realm)
                .clientId(clintId)
                .grantType(OAuth2Constants.PASSWORD)
                .username(username)
                .password(password)
                .resteasyClient(new ResteasyClientBuilderImpl()
                        .connectionPoolSize(10).build())
                .build();
    }

    @Override
    public AuthResponse getAuthResponse(String username, String password) {
        try (var userKeycloak = getKeycloak(username, password)) {
            String accessToken = userKeycloak.tokenManager().getAccessTokenString();
            String refreshToken = userKeycloak.tokenManager().refreshToken().getRefreshToken();
            return AuthResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .build();
        } catch (Exception e) {
            log.error("Error occurred in getting tokens: ", e);
            throw e;
        }
    }

    @Override
    public UserResource getUserById(String id) {
        return getUsersResource().get(id);
    }
}
