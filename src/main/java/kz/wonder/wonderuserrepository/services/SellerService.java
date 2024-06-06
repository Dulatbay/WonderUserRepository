package kz.wonder.wonderuserrepository.services;

import kz.wonder.wonderuserrepository.dto.request.SellerRegistrationRequest;
import kz.wonder.wonderuserrepository.dto.request.SellerUserUpdateRequest;
import kz.wonder.wonderuserrepository.entities.WonderUser;

import java.io.IOException;

public interface SellerService {
    void createSellerUser(SellerRegistrationRequest sellerRegistrationRequest);

    WonderUser updateUser(Long id, SellerUserUpdateRequest sellerUserUpdateRequest);

    String generateOfSellersXmlByKeycloakId(String userId) throws IOException, javax.xml.bind.JAXBException;
}
