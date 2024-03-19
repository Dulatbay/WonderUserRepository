package kz.wonder.wonderuserrepository.controllers;

import jakarta.validation.Valid;
import kz.wonder.kaspi.client.api.KaspiApi;
import kz.wonder.wonderuserrepository.constants.Utils;
import kz.wonder.wonderuserrepository.dto.request.KaspiStoreCreateRequest;
import kz.wonder.wonderuserrepository.dto.response.StoreResponse;
import kz.wonder.wonderuserrepository.services.KaspiStoreService;
import kz.wonder.wonderuserrepository.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/stores")
public class StoreController {
    private final KaspiStoreService kaspiStoreService;
    private final UserService userService;
    private final KaspiApi kaspiApi;

    @PostMapping()
    public ResponseEntity<Void> createStore(        @RequestBody
                                                    @Valid
                                                    KaspiStoreCreateRequest kaspiStoreCreateRequest) {
        var token = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var userId = Utils.extractIdFromToken(token);

        log.info("userId: {}", userId);

        var user = userService.getUserByKeycloakId(userId);

        kaspiStoreCreateRequest.setUser(user);
        kaspiStoreService.createStore(kaspiStoreCreateRequest);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping()
    public ResponseEntity<List<StoreResponse>> getAllOwnStores() {
        var token = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var userId = Utils.extractIdFromToken(token);

        log.info("userId: {}", userId);


        return null;
    }

}
