package kz.wonder.wonderuserrepository.services.impl;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import kz.wonder.kaspi.client.api.KaspiApi;
import kz.wonder.wonderuserrepository.dto.request.SellerRegistrationRequest;
import kz.wonder.wonderuserrepository.entities.KaspiToken;
import kz.wonder.wonderuserrepository.dto.KeycloakBaseUser;
import kz.wonder.wonderuserrepository.entities.WonderUser;
import kz.wonder.wonderuserrepository.exceptions.DbObjectNotFoundException;
import kz.wonder.wonderuserrepository.repositories.KaspiTokenRepository;
import kz.wonder.wonderuserrepository.repositories.UserRepository;
import kz.wonder.wonderuserrepository.security.keycloak.KeycloakRole;
import kz.wonder.wonderuserrepository.services.KeycloakService;
import kz.wonder.wonderuserrepository.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

	private final KaspiTokenRepository kaspiTokenRepository;
	private final UserRepository userRepository;
	private final KeycloakService keycloakService;
	private final KaspiApi kaspiApi;
	private final EntityManager entityManager;

	@Value("${application.kaspi-token}")
	private String apiToken;

	@Override
	public void createSellerUser(SellerRegistrationRequest sellerRegistrationRequest) {
		if (!isTokenValid(sellerRegistrationRequest.getTokenKaspi()))
			throw new IllegalArgumentException("Token is invalid");
		if (userRepository.existsByPhoneNumber(sellerRegistrationRequest.getPhoneNumber()))
			throw new IllegalArgumentException("Phone number must be unique");
		if (kaspiTokenRepository.existsBySellerId(sellerRegistrationRequest.getSellerId()))
			throw new IllegalArgumentException("Seller id must be unique");

		WonderUser wonderUser = new WonderUser();
		wonderUser.setPhoneNumber(sellerRegistrationRequest.getPhoneNumber());
		wonderUser.setKeycloakId(sellerRegistrationRequest.getKeycloakId());

		KaspiToken kaspiToken = new KaspiToken();
		kaspiToken.setEnabled(true);
		kaspiToken.setSellerName(sellerRegistrationRequest.getSellerName());
		kaspiToken.setSellerId(sellerRegistrationRequest.getSellerId());
		kaspiToken.setToken(sellerRegistrationRequest.getTokenKaspi());
		kaspiToken.setWonderUser(wonderUser);
		userRepository.save(wonderUser);
		// todo: возвращает 401 если token is null
		kaspiTokenRepository.save(kaspiToken);
	}


	@Override
	public WonderUser getUserByKeycloakId(String keycloakId) {
		return userRepository.findByKeycloakId(keycloakId)
				.orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase(), "WonderUser doesn't exist"));

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
							if (user.getEmail().equals("tester@mail.ru")) {
								testerUserId.set(user.getId());
								return false;
							}

							return usersFromDB
									.stream()
									.noneMatch(user1 -> user1.getKeycloakId()
											.equals(user.getId()))
									&&
									!user.getUsername().equals("admin_qit");
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
				keycloakUser.setEmail("tester@mail.ru");
				keycloakUser.setPassword("test_tester");
				keycloakUser.setFirstName("test");
				keycloakUser.setLastName("test");
				var keycloakTester = keycloakService.createUserByRole(keycloakUser,
						KeycloakRole.SUPER_ADMIN
				);
				testerUserId.set(keycloakTester.getId());
			}

			var wonderUser = new WonderUser();
			wonderUser.setKeycloakId(testerUserId.get());
			wonderUser.setPhoneNumber("tester");

			KaspiToken kaspiToken = new KaspiToken();
			kaspiToken.setEnabled(true);
			kaspiToken.setSellerName("Tester");
			kaspiToken.setSellerId(testerUserId.get());
			kaspiToken.setToken(apiToken);
			kaspiToken.setWonderUser(wonderUser);

			userRepository.save(wonderUser);
			kaspiTokenRepository.save(kaspiToken);
			log.info("New tester created");
		}


	}


	private boolean isTokenValid(String token) {
//        try {
//            kaspiApi.getDataCitiesWithToken(token);
//            return true;
//        }catch (Exception e) {
//            log.info("Exception: ", e);
//            return false;
//        }
		return true;
	}
}
