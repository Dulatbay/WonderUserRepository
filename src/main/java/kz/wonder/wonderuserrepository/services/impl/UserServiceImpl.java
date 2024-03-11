package kz.wonder.wonderuserrepository.services.impl;

import kz.wonder.wonderuserrepository.dto.request.SellerRegistrationRequest;
import kz.wonder.wonderuserrepository.entities.KaspiStore;
import kz.wonder.wonderuserrepository.entities.User;
import kz.wonder.wonderuserrepository.repositories.KaspiStoreRepository;
import kz.wonder.wonderuserrepository.repositories.UserRepository;
import kz.wonder.wonderuserrepository.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final KaspiStoreRepository kaspiStoreRepository;
    private final UserRepository userRepository;
    @Override
    public void createUser(SellerRegistrationRequest sellerRegistrationRequest) {
        if(!isTokenValid(sellerRegistrationRequest.getTokenKaspi()))
            throw new IllegalArgumentException("Token is invalid");

        User user = new User();
        user.setPhoneNumber(sellerRegistrationRequest.getPhoneNumber());
        user.setKeycloakId(sellerRegistrationRequest.getKeycloakId());

        KaspiStore kaspiStore = new KaspiStore();
        kaspiStore.setEnabled(true);
        kaspiStore.setSellerName(sellerRegistrationRequest.getSellerName());
        kaspiStore.setSellerId(sellerRegistrationRequest.getSellerId());
        kaspiStore.setToken(sellerRegistrationRequest.getTokenKaspi());
        kaspiStore.setUser(user);

        userRepository.save(user);
        kaspiStoreRepository.save(kaspiStore);
    }

    private boolean isTokenValid(String token){
        return true;
    }
}
