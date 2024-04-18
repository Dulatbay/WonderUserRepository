package kz.wonder.wonderuserrepository.controllers;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import kz.wonder.wonderuserrepository.dto.request.SellerRegistrationRequest;
import kz.wonder.wonderuserrepository.dto.request.UserAuthRequest;
import kz.wonder.wonderuserrepository.dto.response.AuthResponse;
import kz.wonder.wonderuserrepository.dto.response.MessageResponse;
import kz.wonder.wonderuserrepository.security.keycloak.KeycloakRole;
import kz.wonder.wonderuserrepository.services.KeycloakService;
import kz.wonder.wonderuserrepository.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
@Slf4j
public class AuthController {

    private final KeycloakService keycloakService;
    private final UserService userService;

    @Operation(summary = "Registration")
    @PostMapping("/registration")
    public ResponseEntity<MessageResponse> registrationAsSeller(@RequestBody
                                                                @Valid
                                                                SellerRegistrationRequest registrationRequestBody) {
        var userRepresentation = keycloakService.createUserByRole(registrationRequestBody, KeycloakRole.SELLER);
        registrationRequestBody.setKeycloakId(userRepresentation.getId());
        try {
            userService.createSellerUser(registrationRequestBody);
        } catch (Exception e) {
            log.info("Error while creating seller");
            keycloakService.deleteUserById(userRepresentation.getId());
            throw e;
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(new MessageResponse("Подтвердите почту чтобы продолжить"));
    }


    @Operation(summary = "Login")
    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid UserAuthRequest userAuthRequest) {
        return ResponseEntity.ok(keycloakService.getAuthResponse(userAuthRequest.email(), userAuthRequest.password()));
    }
}
