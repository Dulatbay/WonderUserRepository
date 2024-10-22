package kz.wonder.wonderuserrepository.controllers;

import jakarta.validation.Valid;
import kz.wonder.wonderuserrepository.constants.Utils;
import kz.wonder.wonderuserrepository.dto.request.StoreCellChangeRequest;
import kz.wonder.wonderuserrepository.dto.request.StoreCellCreateRequest;
import kz.wonder.wonderuserrepository.dto.response.StoreCellResponse;
import kz.wonder.wonderuserrepository.security.authorizations.AccessForAdmins;
import kz.wonder.wonderuserrepository.services.StoreCellService;
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
    @AccessForAdmins
    public ResponseEntity<Void> storeCell(@RequestBody @Valid StoreCellCreateRequest storeCellCreateRequest) {
        var token = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var keycloakId = Utils.extractIdFromToken(token);

        storeCellService.create(storeCellCreateRequest, keycloakId);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/add-product-to-cell")
    @AccessForAdmins
    public ResponseEntity<Void> addProductToCell(@RequestParam("cell-id") Long cellId,
                                                 @RequestParam("product-article") String productArticle) {
        var token = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var keycloakId = Utils.extractIdFromToken(token);

        storeCellService.addProductToCell(cellId, productArticle, keycloakId);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping
    @AccessForAdmins
    public ResponseEntity<List<StoreCellResponse>> getAllStoreCells(@RequestParam(value = "store-id") Long storeId) {
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

    @PutMapping("{cellId}")
    public ResponseEntity<Void> changeStoreCell(@PathVariable Long cellId, @RequestBody @Valid StoreCellChangeRequest storeCellChangeRequest) {
        var token = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var keycloakId = Utils.extractIdFromToken(token);

        storeCellService.changeStoreCell(keycloakId, cellId, storeCellChangeRequest);

        return ResponseEntity.noContent().build();
    }

}
