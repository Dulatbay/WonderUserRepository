package kz.wonder.wonderuserrepository.services;

import kz.wonder.wonderuserrepository.dto.request.StoreEmployeeUpdatePassword;
import kz.wonder.wonderuserrepository.dto.response.AuthResponse;
import kz.wonder.wonderuserrepository.entities.KeycloakBaseUser;
import kz.wonder.wonderuserrepository.security.keycloak.KeycloakRole;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.UserRepresentation;

import java.util.List;

public interface KeycloakService {
	UserRepresentation createUserByRole(KeycloakBaseUser keycloakBaseUser, KeycloakRole keycloakRole);

	AuthResponse getAuthResponse(String email, String password);

	void deleteUserById(String userId);

	List<UserRepresentation> getAllUsers();

	List<UserRepresentation> getAllUsersByRole(KeycloakRole keycloakRole);

	UserResource getUserById(String id);

	UserResource updateUser(KeycloakBaseUser keycloakBaseUser);

	void updatePassword(String keycloakId, StoreEmployeeUpdatePassword updatePassword);
}
