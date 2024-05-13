package kz.wonder.wonderuserrepository.controllers;

import kz.wonder.wonderuserrepository.dto.response.SellerUserResponse;
import kz.wonder.wonderuserrepository.mappers.UserMapper;
import kz.wonder.wonderuserrepository.services.KeycloakService;
import kz.wonder.wonderuserrepository.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;
    private final KeycloakService keycloakService;
    private final UserMapper userMapper;

    @GetMapping("/sellers/{id}")
    public ResponseEntity<SellerUserResponse> getSellerUserById(@PathVariable Long id) {

        var wonderUser = userService.getUserById(id);

        var keycloakUser = keycloakService.getUserById(wonderUser.getKeycloakId());

        var result = userMapper.toUserResponse(wonderUser, keycloakUser.toRepresentation(), wonderUser.getKaspiToken());

        return ResponseEntity.ok(result);
    }
}
