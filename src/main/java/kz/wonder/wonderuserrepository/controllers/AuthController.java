package kz.wonder.wonderuserrepository.controllers;

import kz.wonder.wonderuserrepository.dto.response.AuthResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {
    @PostMapping("/registration")
    public ResponseEntity<AuthResponse> registration() {


        return ResponseEntity.status(HttpStatus.CREATED).body(AuthResponse.builder().build());
    }
}
