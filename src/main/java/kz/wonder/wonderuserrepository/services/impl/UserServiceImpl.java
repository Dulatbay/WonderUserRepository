package kz.wonder.wonderuserrepository.services.impl;

import jakarta.transaction.Transactional;
import kz.wonder.kaspi.client.api.KaspiApi;
import kz.wonder.wonderuserrepository.dto.request.SellerRegistrationRequest;
import kz.wonder.wonderuserrepository.entities.KaspiToken;
import kz.wonder.wonderuserrepository.entities.User;
import kz.wonder.wonderuserrepository.exceptions.DbObjectNotFoundException;
import kz.wonder.wonderuserrepository.repositories.KaspiTokenRepository;
import kz.wonder.wonderuserrepository.repositories.UserRepository;
import kz.wonder.wonderuserrepository.services.KeycloakService;
import kz.wonder.wonderuserrepository.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final KaspiTokenRepository kaspiTokenRepository;
    private final UserRepository userRepository;
    private final KeycloakService keycloakService;
    private final KaspiApi kaspiApi;

    @Override
    public void createUser(SellerRegistrationRequest sellerRegistrationRequest) {
        if (!isTokenValid(sellerRegistrationRequest.getTokenKaspi()))
            throw new IllegalArgumentException("Token is invalid");
        if (userRepository.existsByPhoneNumber(sellerRegistrationRequest.getPhoneNumber()))
            throw new IllegalArgumentException("Phone number must be unique");
        if (kaspiTokenRepository.existsBySellerId(sellerRegistrationRequest.getSellerId()))
            throw new IllegalArgumentException("Seller id must be unique");

        User user = new User();
        user.setPhoneNumber(sellerRegistrationRequest.getPhoneNumber());
        user.setKeycloakId(sellerRegistrationRequest.getKeycloakId());

        KaspiToken kaspiToken = new KaspiToken();
        kaspiToken.setEnabled(true);
        kaspiToken.setSellerName(sellerRegistrationRequest.getSellerName());
        kaspiToken.setSellerId(sellerRegistrationRequest.getSellerId());
        kaspiToken.setToken(sellerRegistrationRequest.getTokenKaspi());
        kaspiToken.setUser(user);
        userRepository.save(user);
        kaspiTokenRepository.save(kaspiToken);
    }

    @Override
    public User getUserByKeycloakId(String keycloakId) {
        return userRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.BAD_REQUEST.getReasonPhrase(), "User doesn't exist"));

    }


    @Override
    @Transactional
    public void syncUsersBetweenDBAndKeycloak() {
        var usersFromDB = userRepository.findAll();

        var usersFromKeycloak = keycloakService.getAllUsers();

        var usersToDeleteFromKeycloak = usersFromKeycloak.stream()
                .filter(user -> usersFromDB
                        .stream()
                        .noneMatch(user1 -> user1.getKeycloakId()
                                .equals(user.getId()))
                        &&
                        !user.getUsername().equals("admin_qit")
                )
                .toList();

        for (var user : usersToDeleteFromKeycloak) {
            keycloakService.deleteUserById(user.getId());
        }

        List<User> usersToDeleteFromDB = usersFromDB.stream()
                .filter(user -> usersFromKeycloak
                        .stream()
                        .noneMatch(userRepresentation -> userRepresentation.getId().equals(user.getKeycloakId())))
                .toList();

        userRepository.deleteAll(usersToDeleteFromDB);
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
