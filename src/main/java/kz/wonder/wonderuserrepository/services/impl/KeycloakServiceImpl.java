package kz.wonder.wonderuserrepository.services.impl;

import kz.wonder.wonderuserrepository.dto.request.StoreEmployeeUpdatePassword;
import kz.wonder.wonderuserrepository.dto.response.AuthResponse;
import kz.wonder.wonderuserrepository.entities.KeycloakBaseUser;
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
import org.springframework.stereotype.Service;

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

	private final Keycloak keycloak;


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
				throw new IllegalArgumentException("Email is incorrect");
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
				throw new InternalServerErrorException("Unknown error");
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
//        getUsersResource().get(userId).sendVerifyEmail();
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
			throw new IllegalArgumentException("Bad credentials");
		} catch (Exception e) {
			log.error("Error occurred in getting tokens: ", e);
			throw e;
		}
	}

	@Override
	public void deleteUserById(String userId) {
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
			throw new IllegalArgumentException("User by email not found");
		}
		UserRepresentation userToUpdate = users.get(0);
		userToUpdate.setFirstName(keycloakBaseUser.getFirstName());
		userToUpdate.setLastName(keycloakBaseUser.getLastName());
		userToUpdate.setEmail(keycloakBaseUser.getEmail());

		UserResource userResource = usersResource.get(userToUpdate.getId());
		userResource.update(userToUpdate);

		return userResource;
	}

	@Override
	public void updatePassword(String keycloakId, StoreEmployeeUpdatePassword updatePassword) {
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
			throw new IllegalArgumentException("Old password is incorrect");
		}
	}
}
