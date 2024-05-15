package kz.wonder.wonderuserrepository.controllers;

import kz.wonder.wonderuserrepository.dto.request.SellerUserUpdateRequest;
import kz.wonder.wonderuserrepository.dto.response.SellerUserResponse;
import kz.wonder.wonderuserrepository.mappers.UserMapper;
import kz.wonder.wonderuserrepository.security.keycloak.KeycloakBaseUser;
import kz.wonder.wonderuserrepository.services.KeycloakService;
import kz.wonder.wonderuserrepository.services.SellerService;
import kz.wonder.wonderuserrepository.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/sellers")
public class SellerController {
    private final UserService userService;
    private final KeycloakService keycloakService;
    private final UserMapper userMapper;
    private final SellerService sellerService;

    @GetMapping("{id}")
    public ResponseEntity<SellerUserResponse> getSellerUserById(@PathVariable Long id) {

        var wonderUser = userService.getUserById(id);

        var keycloakUser = keycloakService.getUserById(wonderUser.getKeycloakId());

        var result = userMapper.toUserResponse(wonderUser, keycloakUser.toRepresentation(), wonderUser.getKaspiToken());

        return ResponseEntity.ok(result);
    }

    @PutMapping("{id}")
    public ResponseEntity<SellerUserResponse> updateSellerUserById(@PathVariable Long id, @RequestBody SellerUserUpdateRequest sellerUserUpdateRequest) {
        var keycloakBaseUser = new KeycloakBaseUser();
        keycloakBaseUser.setEmail(sellerUserUpdateRequest.getEmail());
        keycloakBaseUser.setFirstName(sellerUserUpdateRequest.getFirstName());
        keycloakBaseUser.setLastName(sellerUserUpdateRequest.getLastName());

        var userResource = keycloakService.updateUser(keycloakBaseUser).toRepresentation();

        var seller = sellerService.updateUser(id, sellerUserUpdateRequest);

        var result = userMapper.toUserResponse(seller, userResource, seller.getKaspiToken());

        return ResponseEntity.ok(result);
    }
}
