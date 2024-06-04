package kz.wonder.wonderuserrepository.mappers;

import kz.wonder.wonderuserrepository.dto.request.SellerRegistrationRequest;
import kz.wonder.wonderuserrepository.dto.request.SellerUserUpdateRequest;
import kz.wonder.wonderuserrepository.entities.KaspiToken;
import kz.wonder.wonderuserrepository.entities.WonderUser;
import org.springframework.stereotype.Component;

@Component
public class SellerMapper {
    public WonderUser toCreateWonderUser(SellerRegistrationRequest sellerRegistrationRequest) {
        WonderUser wonderUser = new WonderUser();
        wonderUser.setPhoneNumber(sellerRegistrationRequest.getPhoneNumber());
        wonderUser.setKeycloakId(sellerRegistrationRequest.getKeycloakId());
        wonderUser.setUsername(sellerRegistrationRequest.getFirstName() + " " + sellerRegistrationRequest.getLastName());
        return wonderUser;
    }

    public KaspiToken toCreateKaspiToken(SellerRegistrationRequest sellerRegistrationRequest, WonderUser wonderUser) {
        KaspiToken kaspiToken = new KaspiToken();
        kaspiToken.setEnabled(true);
        kaspiToken.setSellerName(sellerRegistrationRequest.getSellerName());
        kaspiToken.setSellerId(sellerRegistrationRequest.getSellerId());
        kaspiToken.setToken(sellerRegistrationRequest.getTokenKaspi());
        kaspiToken.setWonderUser(wonderUser);

        return kaspiToken;
    }

    public void toUpdateUser(WonderUser existingUser, SellerUserUpdateRequest sellerUserUpdateRequest) {
        final var kaspiToken = existingUser.getKaspiToken();

        existingUser.setUsername(sellerUserUpdateRequest.getFirstName() + " " + sellerUserUpdateRequest.getLastName());
        existingUser.setPhoneNumber(sellerUserUpdateRequest.getPhoneNumber());
        existingUser.setPhoneNumber(sellerUserUpdateRequest.getPhoneNumber());

        kaspiToken.setSellerName(sellerUserUpdateRequest.getSellerName());
        kaspiToken.setSellerId(sellerUserUpdateRequest.getSellerId());
        kaspiToken.setToken(sellerUserUpdateRequest.getTokenKaspi());
    }
}
