package kz.wonder.wonderuserrepository.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import kz.wonder.wonderuserrepository.constants.Utils;
import kz.wonder.wonderuserrepository.dto.request.SellerRegistrationRequest;
import kz.wonder.wonderuserrepository.dto.request.SellerUserUpdateRequest;
import kz.wonder.wonderuserrepository.dto.request.UpdatePasswordRequest;
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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
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

    @GetMapping("/me")
    @Operation(summary = "Get seller user by session", description = "This endpoint returns the seller by session")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the seller user by session")
    })
    public ResponseEntity<SellerUserResponse> getSellerUserBySession() {
        var token = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var keycloakId = Utils.extractIdFromToken(token);

        var wonderUser = userService.getUserByKeycloakId(keycloakId);
        var keycloakUser = keycloakService.getUserById(wonderUser.getKeycloakId());

        var result = userMapper.toUserResponse(wonderUser, keycloakUser.toRepresentation(), wonderUser.getKaspiToken());

        return ResponseEntity.ok(result);
    }

    @PatchMapping("/update-password")
    @Operation(summary = "Update seller's password", description = "This endpoint updates the seller's password")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully updated the seller's password")
    })
    public ResponseEntity<Void> updateSellerUserById(@RequestBody UpdatePasswordRequest updatePasswordRequest) {
        var token = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var keycloakId = Utils.extractIdFromToken(token);

        var keycloakUser = keycloakService.getUserById(keycloakId).toRepresentation();

        updatePasswordRequest.setEmail(keycloakUser.getEmail());
        keycloakService.updatePassword(keycloakUser.getId(), updatePasswordRequest);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PutMapping("{id}")
    @Operation(summary = "Update seller information by ID", description = "This endpoint updates the current information about seller by specific ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully updated the seller's account details")
    })
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
