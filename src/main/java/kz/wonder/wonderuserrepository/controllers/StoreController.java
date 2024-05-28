package kz.wonder.wonderuserrepository.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import kz.wonder.wonderuserrepository.constants.Utils;
import kz.wonder.wonderuserrepository.dto.request.KaspiStoreChangeRequest;
import kz.wonder.wonderuserrepository.dto.request.KaspiStoreCreateRequest;
import kz.wonder.wonderuserrepository.dto.response.StoreDetailResponse;
import kz.wonder.wonderuserrepository.dto.response.StoreResponse;
import kz.wonder.wonderuserrepository.security.keycloak.KeycloakRole;
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

import static kz.wonder.wonderuserrepository.constants.Utils.extractIdFromToken;
import static kz.wonder.wonderuserrepository.constants.Utils.getAuthorities;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/stores")
public class StoreController {
    private final KaspiStoreService kaspiStoreService;
    private final UserService userService;

    @PostMapping()
    @Operation(summary = "Create new store", description = "This endpoint allows to create a new store")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully created new store")
    })
    public ResponseEntity<Void> createStore(@RequestBody
                                            @Valid
                                            KaspiStoreCreateRequest kaspiStoreCreateRequest) {
        kaspiStoreCreateRequest.getDayOfWeekWorks()
                .forEach(i -> {
                    if (i.numericDayOfWeek() < 1 || i.numericDayOfWeek() > 7)
                        throw new IllegalArgumentException("Number of week must be in range 1-7");
                });

        var token = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var userId = Utils.extractIdFromToken(token);

        var user = userService.getUserByKeycloakId(userId);

        kaspiStoreCreateRequest.setWonderUser(user);
        kaspiStoreService.createStore(kaspiStoreCreateRequest);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }


    @DeleteMapping("/{id}")
    @Operation(summary = "Delete store by id", description = "This endpoint deletes the store by its own id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully deleted the store by ID")
    })
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        var token = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();

        if (getAuthorities(token.getAuthorities())
                .contains(KeycloakRole.SUPER_ADMIN.name())) {
            kaspiStoreService.deleteById(id);
        } else {
            kaspiStoreService.deleteById(id, Utils.extractIdFromToken(token));
        }
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update the specific store by ID", description = "This endpoint allows to update details about the store")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully updated the details about store by ID")
    })
    public ResponseEntity<Void> changeStore(@RequestBody
                                            @Valid
                                            KaspiStoreChangeRequest changeRequest,
                                            @PathVariable Long id) {
        var token = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();

        if (getAuthorities(token.getAuthorities())
                .contains(KeycloakRole.SUPER_ADMIN.name())) {
            kaspiStoreService.changeStore(changeRequest, id);
        } else {
            var userId = Utils.extractIdFromToken(token);
            kaspiStoreService.changeStore(changeRequest, id, userId);
        }

        return ResponseEntity.ok().build();
    }

    @PostMapping("/add-box-type")
    @Operation(summary = "Add new acceptable box type for the store", description = "This endpoint allows the store to add the type of box it can accept")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully added new box type for the store")
    })
    public ResponseEntity<Void> addBoxTypeToStore(@RequestParam("box-type-id") Long boxTypeId,
                                                  @RequestParam("store-id") Long storeId) {
        var token = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();

        if (getAuthorities(token.getAuthorities())
                .contains(KeycloakRole.SUPER_ADMIN.name())) {
            kaspiStoreService.addBoxTypeToStore(boxTypeId, storeId);
        } else {
            kaspiStoreService.addBoxTypeToStore(boxTypeId, storeId, Utils.extractIdFromToken(token));
        }

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/remove-box-type")
    @Operation(summary = "Delete specific box type for the store", description = "This endpoint allows a store to remove a box type that it will no longer support")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully deleted the box type or the store")
    })
    public ResponseEntity<Void> removeBoxTypeFromStore(@RequestParam("box-type-id") Long boxTypeId,
                                                       @RequestParam("store-id") Long storeId) {
        var token = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        if (getAuthorities(token.getAuthorities()).contains(KeycloakRole.SUPER_ADMIN.name())) {
            kaspiStoreService.removeBoxType(boxTypeId, storeId);
        } else {
            kaspiStoreService.removeBoxType(boxTypeId, storeId, Utils.extractIdFromToken(token));
        }
        return ResponseEntity.noContent().build();

    }

    @GetMapping()
    @Operation(summary = "Get all stores", description = "Retrieves the list of all stores details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved all stores")
    })
    public ResponseEntity<List<StoreResponse>> getAllOwnStores() {
        var token = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var authorities = getAuthorities(token.getAuthorities());

        if (authorities.contains(KeycloakRole.SUPER_ADMIN.name())) {
            return ResponseEntity.ok(kaspiStoreService.getAll());
        }

        if (authorities.contains(KeycloakRole.ADMIN.name())) {
            var userId = Utils.extractIdFromToken(token);
            log.info("userId: {}", userId);

            var stores = kaspiStoreService.getAllByUser(userId);
            return ResponseEntity.ok(stores);
        }

        if (authorities.contains(KeycloakRole.SELLER.name())) {
            List<StoreResponse> stores = kaspiStoreService.getAllForSeller();
            return ResponseEntity.ok(stores);
        }

        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    @GetMapping("{id}")
    @Operation(summary = "Get store by ID", description = "Retrieves the information about the specific store")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the store by ID")
    })
    public ResponseEntity<StoreResponse> getByIdOwnStore(@PathVariable Long id) {
        var token = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var isSuperAdmin = getAuthorities(token.getAuthorities())
                .contains(KeycloakRole.SUPER_ADMIN.name());

        var store = kaspiStoreService.getById(id, isSuperAdmin, Utils.extractIdFromToken(token));

        return ResponseEntity.ok(store);
    }

    @GetMapping("/details")
    @Operation(summary = "Get stores details", description = "Retrieves the list of all stores details with available box types")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the store details")
    })
    public ResponseEntity<List<StoreDetailResponse>> getAllDetailOwnStores() {
        var token = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var authorities = getAuthorities(token.getAuthorities());
        log.info("authorities: {}", authorities);


        if (authorities.contains(KeycloakRole.SELLER.name())) {
            log.info("Get as seller");
            List<StoreDetailResponse> response = kaspiStoreService.getAllDetailForSeller();
            return ResponseEntity.ok(response);
        }

        if (authorities.contains(KeycloakRole.ADMIN.name())) {
            log.info("Get as admin");
            return ResponseEntity.ok(kaspiStoreService.getAllDetailByUser(Utils.extractIdFromToken(token)));
        }

        if (authorities.contains(KeycloakRole.SUPER_ADMIN.name())) {
            log.info("Get as SUPER admin");
            return ResponseEntity.ok(kaspiStoreService.getAllDetail());
        }

        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    @GetMapping("/details/{id}")
    @Operation(summary = "Get specific store details", description = "Retrieves the details with available box types of the specific store")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the store details by ID")
    })
    public ResponseEntity<StoreDetailResponse> getByIdDetailOwnStores(@PathVariable("id") Long storeId) {
        var token = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var isSuperAdmin = getAuthorities(token.getAuthorities())
                .contains(KeycloakRole.SUPER_ADMIN.name());
        var keycloakId = extractIdFromToken(token);

        var store = kaspiStoreService.getByIdAndByUserDetail(storeId, isSuperAdmin, keycloakId);
        return ResponseEntity.ok(store);
    }

}
