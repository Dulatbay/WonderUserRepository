package kz.wonder.wonderuserrepository.services.impl;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import kz.wonder.wonderuserrepository.entities.WonderUser;
import kz.wonder.wonderuserrepository.exceptions.DbObjectNotFoundException;
import kz.wonder.wonderuserrepository.repositories.UserRepository;
import kz.wonder.wonderuserrepository.security.keycloak.KeycloakBaseUser;
import kz.wonder.wonderuserrepository.security.keycloak.KeycloakRole;
import kz.wonder.wonderuserrepository.services.KeycloakService;
import kz.wonder.wonderuserrepository.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
	private final KeycloakService keycloakService;
	private final EntityManager entityManager;
	private final MessageSource messageSource;


	@Value("${application.username}")
	private String keycloakUsername;

	@Value("${application.password}")
	private String keycloakPassword;



	@Override
	public WonderUser getUserByKeycloakId(String keycloakId) {
		log.info("Retrieving user with keycloakId: {}", keycloakId);
		return userRepository.findByKeycloakId(keycloakId)
				.orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase(), messageSource.getMessage("services-impl.user-service-impl.wonder-user-does-not-exist", null, LocaleContextHolder.getLocale())));

	}


	@Override
	@Transactional
	public void syncUsersBetweenDBAndKeycloak() {
		var usersFromDB = userRepository.findAll();

		var usersFromKeycloak = keycloakService.getAllUsers();

		log.info("usersFromKeycloak(with admin account and testers): {}, usersFromDB: {}", usersFromKeycloak.size(), usersFromDB.size());

		AtomicReference<String> testerUserId = new AtomicReference<>("");


		var usersToDeleteFromKeycloak = usersFromKeycloak.stream()
				.filter(user -> {
							if (user.getEmail() != null && user.getEmail().equals(keycloakUsername)) {
								testerUserId.set(user.getId());
								return false;
							}

							return usersFromDB
									.stream()
									.noneMatch(user1 -> user1.getKeycloakId()
											.equals(user.getId()))
									&&
									!user.getUsername().equals(keycloakUsername);
						}
				)
				.toList();

		for (var user : usersToDeleteFromKeycloak) {
			keycloakService.deleteUserById(user.getId());
		}

		AtomicBoolean testUserExists = new AtomicBoolean(false);

		List<WonderUser> usersToDeleteFromDB = usersFromDB
				.stream()
				.filter(user -> {
					if (user.getKeycloakId().equals(testerUserId.get())) {
						testUserExists.set(true);
					}
					return usersFromKeycloak
							.stream()
							.noneMatch(userRepresentation -> userRepresentation
									.getId()
									.equals(user.getKeycloakId()));
				})
				.toList();
		log.info("usersToDeleteFromKeycloak: {}, usersToDeleteFromDB: {}", usersToDeleteFromKeycloak.size(), usersToDeleteFromDB.size());

		if(!usersToDeleteFromDB.isEmpty()){
			userRepository.deleteAll(usersToDeleteFromDB);
			entityManager.flush();
			entityManager.clear();
		}


		log.info("Test user exists in db: {}, test user exists in keycloak: {}", testUserExists.get(), !testerUserId.get().isEmpty());

		log.info("Tester id: {}", testerUserId.get());

		if (!testUserExists.get()) {
			if (testerUserId.get().isEmpty()) {
				var keycloakUser = new KeycloakBaseUser();
				keycloakUser.setEmail(keycloakUsername);
				keycloakUser.setPassword(keycloakPassword);
				keycloakUser.setFirstName("Akhan");
				keycloakUser.setLastName("Dulatbay");
				var keycloakTester = keycloakService.createUserByRole(keycloakUser,
						KeycloakRole.SUPER_ADMIN
				);
				testerUserId.set(keycloakTester.getId());
			}

			var wonderUser = new WonderUser();
			wonderUser.setKeycloakId(testerUserId.get());
			wonderUser.setPhoneNumber("tester");
			wonderUser.setUsername(keycloakUsername);

			userRepository.save(wonderUser);
			log.info("New tester created");
		}
    }
}
