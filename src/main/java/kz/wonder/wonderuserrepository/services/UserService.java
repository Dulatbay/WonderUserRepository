package kz.wonder.wonderuserrepository.services;

import kz.wonder.wonderuserrepository.dto.request.SellerRegistrationRequest;

public interface UserService {
    void createUser(SellerRegistrationRequest sellerRegistrationRequest);
}
