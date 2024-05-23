package kz.wonder.wonderuserrepository.services.impl;

import kz.wonder.kaspi.client.api.KaspiApi;
import kz.wonder.wonderuserrepository.dto.request.SellerRegistrationRequest;
import kz.wonder.wonderuserrepository.dto.request.SellerUserUpdateRequest;
import kz.wonder.wonderuserrepository.entities.KaspiToken;
import kz.wonder.wonderuserrepository.entities.WonderUser;
import kz.wonder.wonderuserrepository.exceptions.DbObjectNotFoundException;
import kz.wonder.wonderuserrepository.repositories.KaspiTokenRepository;
import kz.wonder.wonderuserrepository.repositories.UserRepository;
import kz.wonder.wonderuserrepository.services.SellerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;


@Slf4j
@Service
@RequiredArgsConstructor
public class SellerServiceImpl implements SellerService {
    private final UserRepository userRepository;
    private final KaspiTokenRepository kaspiTokenRepository;
    private final KaspiApi kaspiApi;


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
        wonderUser.setUsername(sellerRegistrationRequest.getFirstName() + " " + sellerRegistrationRequest.getLastName());

        KaspiToken kaspiToken = new KaspiToken();
        kaspiToken.setEnabled(true);
        kaspiToken.setSellerName(sellerRegistrationRequest.getSellerName());
        kaspiToken.setSellerId(sellerRegistrationRequest.getSellerId());
        kaspiToken.setToken(sellerRegistrationRequest.getTokenKaspi());
        kaspiToken.setWonderUser(wonderUser);
        userRepository.save(wonderUser);

        log.info("Created User with id {}\nCreated Kaspi token with id {}", wonderUser.getId(), kaspiToken.getId());

        // todo: возвращает 401 если token is null
        kaspiTokenRepository.save(kaspiToken);
    }

    @Override
    public WonderUser updateUser(Long id, SellerUserUpdateRequest sellerUserUpdateRequest) {
        final var user = userRepository.findById(id)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase(), "User with id " + id + " not found"));

        final var kaspiToken = user.getKaspiToken();

        user.setUsername(sellerUserUpdateRequest.getFirstName() + " " + sellerUserUpdateRequest.getLastName());
        user.setPhoneNumber(sellerUserUpdateRequest.getPhoneNumber());
        user.setPhoneNumber(sellerUserUpdateRequest.getPhoneNumber());
        kaspiToken.setSellerName(sellerUserUpdateRequest.getSellerName());
        kaspiToken.setSellerId(sellerUserUpdateRequest.getSellerId());
        kaspiToken.setToken(sellerUserUpdateRequest.getTokenKaspi());

        return userRepository.save(user);
    }

    private boolean isTokenValid(String token) {
        try {
            kaspiApi.getDataCitiesWithToken(token);
            return true;
        } catch (Exception e) {
            log.info("Exception: ", e);
            return false;
        }
    }
}
