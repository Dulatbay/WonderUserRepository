package kz.wonder.wonderuserrepository.services;

import kz.wonder.wonderuserrepository.dto.request.SellerRegistrationRequest;
import kz.wonder.wonderuserrepository.dto.request.SellerUserUpdateRequest;
import kz.wonder.wonderuserrepository.entities.WonderUser;

public interface SellerService {
    void createSellerUser(SellerRegistrationRequest sellerRegistrationRequest);

    WonderUser updateUser(Long id, SellerUserUpdateRequest sellerUserUpdateRequest);
}
