package kz.wonder.wonderuserrepository.controllers;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import kz.wonder.wonderuserrepository.dto.request.SellerRegistrationRequest;
import kz.wonder.wonderuserrepository.dto.request.SellerUserUpdateRequest;
import kz.wonder.wonderuserrepository.dto.response.MessageResponse;
import kz.wonder.wonderuserrepository.dto.response.SellerUserResponse;
import kz.wonder.wonderuserrepository.mappers.UserMapper;
import kz.wonder.wonderuserrepository.security.keycloak.KeycloakBaseUser;
import kz.wonder.wonderuserrepository.security.keycloak.KeycloakRole;
import kz.wonder.wonderuserrepository.services.KeycloakService;
import kz.wonder.wonderuserrepository.services.SellerService;
import kz.wonder.wonderuserrepository.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@Slf4j
@RequestMapping("/sellers")
public class SellerController {
    private final UserService userService;
    private final KeycloakService keycloakService;
    private final UserMapper userMapper;
    private final SellerService sellerService;

    // todo: check in security by role

    @GetMapping("/{keycloakId}")
    public ResponseEntity<SellerUserResponse> getSellerUserById(@PathVariable String keycloakId) {
        var wonderUser = userService.getUserByKeycloakId(keycloakId);


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

    @Operation(summary = "Registration")
    @PostMapping("/registration")
    public ResponseEntity<MessageResponse> registrationAsSeller(@RequestBody
                                                                @Valid
                                                                SellerRegistrationRequest registrationRequestBody) {
        var userRepresentation = keycloakService.createUserByRole(registrationRequestBody, KeycloakRole.SELLER);
        registrationRequestBody.setKeycloakId(userRepresentation.getId());
        try {
            sellerService.createSellerUser(registrationRequestBody);
        } catch (Exception e) {
            log.info("Error while creating seller");
            keycloakService.deleteUserById(userRepresentation.getId());
            throw e;
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(new MessageResponse("Подтвердите почту чтобы продолжить"));
    }

}
