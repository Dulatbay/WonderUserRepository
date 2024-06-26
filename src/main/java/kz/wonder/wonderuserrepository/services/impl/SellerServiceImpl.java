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
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
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
    private final MessageSource messageSource;


    @Override
    public void createSellerUser(SellerRegistrationRequest sellerRegistrationRequest) {
        if (!isTokenValid(sellerRegistrationRequest.getTokenKaspi()))
            throw new IllegalArgumentException(messageSource.getMessage("services-impl.seller-service-impl.invalid-token", null, LocaleContextHolder.getLocale()));
        if (userRepository.existsByPhoneNumber(sellerRegistrationRequest.getPhoneNumber()))
            throw new IllegalArgumentException(messageSource.getMessage("services-impl.seller-service-impl.phone-number-must-be-unique", null, LocaleContextHolder.getLocale()));
        if (kaspiTokenRepository.existsBySellerId(sellerRegistrationRequest.getSellerId()))
            throw new IllegalArgumentException(messageSource.getMessage("services-impl.seller-service-impl.seller-id-must-be-unique", null, LocaleContextHolder.getLocale()));
        if (kaspiTokenRepository.existsByToken(sellerRegistrationRequest.getTokenKaspi()))
            throw new IllegalArgumentException(messageSource.getMessage("services-impl.seller-service-impl.token-must-be-unique", null, LocaleContextHolder.getLocale()));

        WonderUser wonderUser = sellerMapper.toCreateWonderUser(sellerRegistrationRequest);
        KaspiToken kaspiToken = sellerMapper.toCreateKaspiToken(sellerRegistrationRequest, wonderUser);

        userRepository.save(wonderUser);

        kaspiTokenRepository.save(kaspiToken);



        log.info("Created User with id {}\tCreated Kaspi token with id {}", wonderUser.getId(), kaspiToken.getId());
    }

    @Override
    public WonderUser updateUser(Long id, SellerUserUpdateRequest sellerUserUpdateRequest) {
        final var user = userRepository.findById(id)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase(), messageSource.getMessage("services-impl.seller-service-impl.user-with-id", null, LocaleContextHolder.getLocale()) + " " + id + " "  + messageSource.getMessage("services-impl.seller-service-impl.not-found", null, LocaleContextHolder.getLocale())));

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
