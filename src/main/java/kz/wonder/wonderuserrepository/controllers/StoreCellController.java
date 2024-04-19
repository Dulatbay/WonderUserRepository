package kz.wonder.wonderuserrepository.controllers;

import kz.wonder.wonderuserrepository.constants.Utils;
import kz.wonder.wonderuserrepository.dto.request.StoreCellCreateRequest;
import kz.wonder.wonderuserrepository.dto.response.StoreCellResponse;
import kz.wonder.wonderuserrepository.services.StoreCellService;
import kz.wonder.wonderuserrepository.services.impl.StoreCellServiceImpl;
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
@RequestMapping("/cells")
public class StoreCellController {
    private final StoreCellService storeCellService;

    @PostMapping
    public ResponseEntity<Void> storeCell(@RequestBody StoreCellCreateRequest storeCellCreateRequest) {
        var token = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var keycloakId = Utils.extractIdFromToken(token);

        storeCellService.create(storeCellCreateRequest, keycloakId);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping
    public ResponseEntity<List<StoreCellResponse>> getAllStoreCells(@RequestParam(value = "store-id", required = false) Long storeId) {
        var token = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var keycloakId = Utils.extractIdFromToken(token);

        List<StoreCellResponse> storeCellResponses = storeCellService.getAllByParams(storeId, keycloakId);

        return ResponseEntity.ok(storeCellResponses);
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteStoreCell(@PathVariable("id") Long cellId) {
        var token = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var keycloakId = Utils.extractIdFromToken(token);


        storeCellService.delete(cellId, keycloakId);
        return ResponseEntity.noContent().build();
    }

}
