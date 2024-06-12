package kz.wonder.wonderuserrepository.services.impl;

import kz.wonder.kaspi.client.api.KaspiApi;
import kz.wonder.wonderuserrepository.dto.request.SellerRegistrationRequest;
import kz.wonder.wonderuserrepository.dto.request.SellerUserUpdateRequest;
import kz.wonder.wonderuserrepository.entities.KaspiToken;
import kz.wonder.wonderuserrepository.entities.WonderUser;
import kz.wonder.wonderuserrepository.exceptions.DbObjectNotFoundException;
import kz.wonder.wonderuserrepository.mappers.SellerMapper;
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
    private final SellerMapper sellerMapper;


    @Override
    public void createSellerUser(SellerRegistrationRequest sellerRegistrationRequest) {
        if (!isTokenValid(sellerRegistrationRequest.getTokenKaspi()))
            throw new IllegalArgumentException("Токен недействителен");
        if (userRepository.existsByPhoneNumber(sellerRegistrationRequest.getPhoneNumber()))
            throw new IllegalArgumentException("Номер телефона должен быть уникальным");
        if (kaspiTokenRepository.existsBySellerId(sellerRegistrationRequest.getSellerId()))
            throw new IllegalArgumentException("ID продавца должен быть уникальным.");
        if (kaspiTokenRepository.existsByToken(sellerRegistrationRequest.getTokenKaspi()))
            throw new IllegalArgumentException("Токен должен быть уникальным");

        WonderUser wonderUser = sellerMapper.toCreateWonderUser(sellerRegistrationRequest);
        KaspiToken kaspiToken = sellerMapper.toCreateKaspiToken(sellerRegistrationRequest, wonderUser);

        kaspiToken = kaspiTokenRepository.save(kaspiToken);

        wonderUser.setKaspiToken(kaspiToken);

        userRepository.save(wonderUser);

        log.info("Created User with id {}\tCreated Kaspi token with id {}", wonderUser.getId(), kaspiToken.getId());
    }

    @Override
    public WonderUser updateUser(Long id, SellerUserUpdateRequest sellerUserUpdateRequest) {
        final var user = userRepository.findById(id)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase(), "Пользователь с id " + id + " не был найден"));

        sellerMapper.toUpdateUser(user, sellerUserUpdateRequest);

        return userRepository.save(user);
    }

    private boolean isTokenValid(String token) {
        try {
            kaspiApi.getDataCitiesWithToken(token).block();
            return true;
        } catch (Exception e) {
            log.info("Exception: ", e);
            return false;
        }
    }
}
