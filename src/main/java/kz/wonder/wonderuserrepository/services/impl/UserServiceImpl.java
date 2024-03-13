package kz.wonder.wonderuserrepository.services.impl;

import kz.wonder.wonderuserrepository.dto.request.SellerRegistrationRequest;
import kz.wonder.wonderuserrepository.entities.KaspiToken;
import kz.wonder.wonderuserrepository.entities.User;
import kz.wonder.wonderuserrepository.repositories.KaspiTokenRepository;
import kz.wonder.wonderuserrepository.repositories.UserRepository;
import kz.wonder.wonderuserrepository.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final KaspiTokenRepository kaspiTokenRepository;
    private final UserRepository userRepository;
    @Override
    public void createUser(SellerRegistrationRequest sellerRegistrationRequest) {
        if(!isTokenValid(sellerRegistrationRequest.getTokenKaspi()))
            throw new IllegalArgumentException("Token is invalid");

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

    private boolean isTokenValid(String token){
        return true;
    }
}
