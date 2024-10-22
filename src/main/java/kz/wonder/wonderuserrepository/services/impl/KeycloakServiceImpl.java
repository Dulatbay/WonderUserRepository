package kz.wonder.wonderuserrepository.services.impl;

import kz.wonder.wonderuserrepository.dto.request.UpdatePasswordRequest;
import kz.wonder.wonderuserrepository.dto.response.AuthResponse;
import kz.wonder.wonderuserrepository.security.keycloak.KeycloakBaseUser;
import kz.wonder.wonderuserrepository.security.keycloak.KeycloakError;
import kz.wonder.wonderuserrepository.security.keycloak.KeycloakRole;
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
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.List;

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

    @Value("${application.username}")
    private String username;

    @Value("${application.password}")
    private String password;

    private final MessageSource messageSource;


    @Override
    public UserRepresentation createUserByRole(KeycloakBaseUser sellerRegistrationRequest, KeycloakRole keycloakRole) {
        UserRepresentation userRepresentation = setupUserRepresentation(sellerRegistrationRequest);
        String userId = null;
        try (Response response = getUsersResource().create(userRepresentation)) {
            handleUnsuccessfulResponse(response);
            userId = CreatedResponseUtil.getCreatedId(response);
            UserResource userResource = setupUserResource(sellerRegistrationRequest, keycloakRole, userId);

            try {
                sendEmail(userId);
            } catch (Exception e) {
                log.error("Exception: ", e);
                throw new IllegalArgumentException(messageSource.getMessage("services-impl.keycloak-service-impl.invalid-email", null, LocaleContextHolder.getLocale()));
            }

            return userResource.toRepresentation();
        } catch (Exception e) {
            handleExceptionAfterUserIdCreated(userId);
            throw e;
        }
    }

    private UserRepresentation setupUserRepresentation(KeycloakBaseUser request) {
        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setEnabled(true);
        userRepresentation.setFirstName(request.getFirstName());
        userRepresentation.setLastName(request.getLastName());
        userRepresentation.setEmail(request.getEmail());
        userRepresentation.setUsername(request.getEmail());
        return userRepresentation;
    }

    private void handleUnsuccessfulResponse(Response response) {
        if (response.getStatus() != 201) {
            log.info("response status: {}", response.getStatus());
            log.info("response entity: {}", response.getEntity());
            if (response.getStatus() == HttpStatus.SC_CONFLICT) {
                var object = response.readEntity(KeycloakError.class);
                throw new IllegalArgumentException(object.getErrorMessage());
            } else {
                throw new InternalServerErrorException(messageSource.getMessage("services-impl.keycloak-service-impl.unknown-error", null, LocaleContextHolder.getLocale()));
            }
        }
    }

    private UserResource setupUserResource(KeycloakBaseUser keycloakBaseUser, KeycloakRole keycloakRole, String userId) {
        UserResource userResource = getUsersResource().get(userId);
        userResource.resetPassword(getPasswordCredential(keycloakBaseUser.getPassword(), false));
        userResource.roles() //
                .clientLevel(getClient().getId())
                .add(Collections.singletonList(getClientRole(getClient(), keycloakRole)));
        return userResource;
    }

    private void handleExceptionAfterUserIdCreated(String userId) {
        if (userId != null) getUsersResource().delete(userId);
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


    private Keycloak getAdminKeycloak() {
        return KeycloakBuilder.builder()
                .serverUrl(keycloakUrl)
                .realm(realm)
                .clientId(clintId)
                .grantType(OAuth2Constants.PASSWORD)
                .username(username)
                .password(password)
                .resteasyClient(new ResteasyClientBuilderImpl()
                        .connectionPoolSize(10)
                        .build())
                .build();
    }


    private RealmResource getRealmResource() {
        return getAdminKeycloak().realm(realm);
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

    private Keycloak getKeycloak(String username, String password) {
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
        } catch (NotAuthorizedException notAuthorizedException) {
            throw new IllegalArgumentException(messageSource.getMessage("services-impl.keycloak-service-impl.invalid-credentials", null, LocaleContextHolder.getLocale()));
        } catch (BadRequestException e) {
            log.error("Bad request exception: ", e);
            throw e;
        } catch (Exception e) {
            log.error("Error occurred in getting tokens: ", e);
            throw e;
        }
    }

    @Override
    public void deleteUserById(String userId) {
        log.info("Deleted user with id {}", userId);
        getUserById(userId).remove();
    }

    @Override
    public List<UserRepresentation> getAllUsers() {
        return getUsersResource().list();
    }

    @Override
    public List<UserRepresentation> getAllUsersByRole(KeycloakRole keycloakRole) {
        return getRealmResource()
                .clients()
                .get(getClient().getId())
                .roles().get(keycloakRole.name())
                .getUserMembers();
    }


    @Override
    public UserResource getUserById(String id) {
        return getUsersResource().get(id);
    }

    @Override
    public UserResource updateUser(KeycloakBaseUser keycloakBaseUser) {
        var usersResource = getUsersResource();
        List<UserRepresentation> users = usersResource.search(keycloakBaseUser.getEmail(), 0, 1);

        if (users.isEmpty()) {
            throw new IllegalArgumentException(messageSource.getMessage("services-impl.keycloak-service-impl.user-not-found-by-email", null, LocaleContextHolder.getLocale()));
        }
        UserRepresentation userToUpdate = users.get(0);
        userToUpdate.setFirstName(keycloakBaseUser.getFirstName());
        userToUpdate.setLastName(keycloakBaseUser.getLastName());
        userToUpdate.setEmail(keycloakBaseUser.getEmail());

        UserResource userResource = usersResource.get(userToUpdate.getId());
        userResource.update(userToUpdate);

        log.info("Update user: {}", userResource);

        return userResource;
    }

    @Override
    public void updatePassword(String keycloakId, UpdatePasswordRequest updatePassword) {
        try {
            var keycloak = getKeycloak(updatePassword.getEmail(), updatePassword.getOldPassword());
            keycloak.tokenManager().getAccessTokenString();

            UserResource userResource = getUsersResource().get(keycloakId);

            CredentialRepresentation newPassword = new CredentialRepresentation();
            newPassword.setType(CredentialRepresentation.PASSWORD);
            newPassword.setValue(updatePassword.getNewPassword());
            newPassword.setTemporary(false);


            userResource.resetPassword(newPassword);
        } catch (NotAuthorizedException e) {
            log.error("Old password is incorrect for user with ID: {}", keycloakId);
            throw new IllegalArgumentException(messageSource.getMessage("services-impl.keycloak-service-impl.incorrect-old-password", null, LocaleContextHolder.getLocale()));
        }
    }
}
