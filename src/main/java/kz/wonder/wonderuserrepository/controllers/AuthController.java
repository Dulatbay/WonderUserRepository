package kz.wonder.wonderuserrepository.controllers;

import jakarta.validation.Valid;
import kz.wonder.wonderuserrepository.dto.request.SellerRegistrationRequest;
import kz.wonder.wonderuserrepository.dto.response.AuthResponse;
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

    @PostMapping("/registration")
    public ResponseEntity<AuthResponse> registration(@RequestBody @Valid SellerRegistrationRequest registrationRequestBody) {
        // todo: create transactions
        var userRepresentation = keycloakService.createUser(registrationRequestBody).toRepresentation();
        registrationRequestBody.setKeycloakId(userRepresentation.getId());
        userService.createUser(registrationRequestBody);
        var authResponse = keycloakService.getAuthResponse(registrationRequestBody.getEmail(), registrationRequestBody.getPassword());
        return ResponseEntity.status(HttpStatus.CREATED).body(authResponse);
    }
}
