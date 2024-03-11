package kz.wonder.wonderuserrepository.services.impl;

import kz.wonder.wonderuserrepository.dto.request.SellerRegistrationRequest;
import kz.wonder.wonderuserrepository.dto.response.AuthResponse;
import kz.wonder.wonderuserrepository.security.KeycloakRole;
import kz.wonder.wonderuserrepository.services.KeycloakService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final RealmResource realmResource = keycloak.realm(realm);
    private final UsersResource usersResource = realmResource.users();
    private final ClientRepresentation client = realmResource.clients()
            .findByClientId(clintId).getFirst();

    @Override
    public UserResource createUser(SellerRegistrationRequest sellerRegistrationRequest) {
        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setEnabled(true);
        userRepresentation.setFirstName(sellerRegistrationRequest.getFirstName());
        userRepresentation.setLastName(sellerRegistrationRequest.getLastName());
        userRepresentation.setEmail(sellerRegistrationRequest.getEmail());

        try (var response = usersResource.create(userRepresentation)) {
            String userId = CreatedResponseUtil.getCreatedId(response);
            UserResource userResource = usersResource.get(userId);
            userResource.resetPassword(getPasswordCredential(sellerRegistrationRequest.getPassword(), false));
            userResource.roles() //
                    .clientLevel(client.getId()).add(Collections.singletonList(getClientRole(client, KeycloakRole.SELLER)));
            return userResource;
        } catch (Exception e) {
            log.error("Error occurred in creating user: ", e);
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
        return realmResource.clients().get(client.getClientId())
                .roles().get(keycloakRole.name()).toRepresentation();
    }

    @Override
    public AuthResponse getAuthResponse(String username, String password) {
        try (var userKeycloak = KeycloakBuilder.builder()
                .serverUrl(keycloakUrl)
                .realm(realm)
                .clientId(clintId)
                .grantType(OAuth2Constants.PASSWORD)
                .username(username)
                .password(password)
                .resteasyClient(new ResteasyClientBuilderImpl()
                        .connectionPoolSize(10).build())
                .build()) {
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
}
