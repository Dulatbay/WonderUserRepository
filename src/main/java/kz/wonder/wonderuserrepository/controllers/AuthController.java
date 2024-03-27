package kz.wonder.wonderuserrepository.controllers;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import kz.wonder.wonderuserrepository.dto.request.SellerRegistrationRequest;
import kz.wonder.wonderuserrepository.dto.request.UserAuthRequest;
import kz.wonder.wonderuserrepository.dto.response.AuthResponse;
import kz.wonder.wonderuserrepository.dto.response.MessageResponse;
import kz.wonder.wonderuserrepository.services.KeycloakService;
import kz.wonder.wonderuserrepository.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final KeycloakService keycloakService;
    private final UserService userService;

    @Operation(summary = "Registration")
    @PostMapping("/registration")
    public ResponseEntity<MessageResponse> registration(@RequestBody @Valid SellerRegistrationRequest registrationRequestBody) {
        // todo: унификация данных с keycloak и с бд
        var userRepresentation = keycloakService.createUser(registrationRequestBody);
        registrationRequestBody.setKeycloakId(userRepresentation.getId());
        try {
            userService.createUser(registrationRequestBody);
        } catch (Exception e) {
            keycloakService.deleteUserById(userRepresentation.getId());
            throw e;
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(new MessageResponse("Подтвердите почту чтобы продолжить"));
    }


    @Operation(summary = "Login")
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid UserAuthRequest userAuthRequest) {
        return ResponseEntity.ok(keycloakService.getAuthResponse(userAuthRequest.email(), userAuthRequest.password()));
    }

}
