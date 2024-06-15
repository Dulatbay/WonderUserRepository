package kz.wonder.wonderuserrepository.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import kz.wonder.wonderuserrepository.constants.Utils;
import kz.wonder.wonderuserrepository.dto.request.SupplyCreateRequest;
import kz.wonder.wonderuserrepository.dto.request.SupplyScanRequest;
import kz.wonder.wonderuserrepository.dto.request.SupplyStateToRejectRequest;
import kz.wonder.wonderuserrepository.dto.response.*;
import kz.wonder.wonderuserrepository.security.authorizations.AccessForAdmins;
import kz.wonder.wonderuserrepository.security.authorizations.AccessForAdminsAndEmployee;
import kz.wonder.wonderuserrepository.security.authorizations.AccessForAdminsAndSellers;
import kz.wonder.wonderuserrepository.security.authorizations.base.SellerAuthorization;
import kz.wonder.wonderuserrepository.security.authorizations.base.StoreEmployeeAuthorization;
import kz.wonder.wonderuserrepository.security.keycloak.KeycloakRole;
import kz.wonder.wonderuserrepository.services.KeycloakService;
import kz.wonder.wonderuserrepository.services.SupplyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static kz.wonder.wonderuserrepository.constants.Utils.extractIdFromToken;
import static kz.wonder.wonderuserrepository.constants.Utils.getAuthorities;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/supplies")
public class SupplyController {

    private final SupplyService supplyService;
    private final KeycloakService keycloakService;

    @PostMapping(value = "/process-file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Process supply file", description = "Processes the uploaded file containing supply data. The file is processed based on the user's ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully proceeded the supply file")
    })
    @SellerAuthorization
    public ResponseEntity<List<SupplyProcessFileResponse>> processFile(@RequestPart("file") MultipartFile file) {
        var token = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var userId = extractIdFromToken(token);
        var result = supplyService.processFile(file, userId);
        return ResponseEntity.ok(result);
    }

    @PostMapping
    @Operation(summary = "Create new supply", description = "Creates a new supply. The supply is created by user's ID and token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully created new supply")
    })
    @SellerAuthorization
    public ResponseEntity<SupplySellerResponse> createSupply(@RequestBody @Valid SupplyCreateRequest createRequest) {
        var token = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var userId = extractIdFromToken(token);
        var supplyCreatedResponse = supplyService.createSupply(createRequest, userId);

        return ResponseEntity.status(HttpStatus.CREATED).body(supplyCreatedResponse);
    }


    @PutMapping("/state")
    @AccessForAdminsAndSellers
        public ResponseEntity<SupplySellerResponse> changeStateToReject(@RequestBody @Valid SupplyStateToRejectRequest stateUpdateRequest){
        var supplyUpdatedResponse = supplyService.updateSupplyStateToReject(stateUpdateRequest);

        return ResponseEntity.status(HttpStatus.OK).body(supplyUpdatedResponse);
    }

    @GetMapping("/admin")
    @Operation(summary = "Get all supplies for admin", description = "Retrieves a list of supplies within the specified date range for the admin")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved a list of supplies for admin")
    })
    @AccessForAdmins
    public ResponseEntity<List<SupplyAdminResponse>> getSuppliesAdmin(@RequestParam("start-date") LocalDate startDate,
                                                                      @RequestParam("end-date") LocalDate endDate
    ) {
        var token = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var id = extractIdFromToken(token);
        var userRepresentation = keycloakService.getUserById(id).toRepresentation();

        List<SupplyAdminResponse> result = supplyService.getSuppliesOfAdmin(startDate, endDate, userRepresentation.getId(), userRepresentation.getFirstName() + " " + userRepresentation.getLastName(), id);
        return ResponseEntity.ok(result);
    }


    @GetMapping("/detail/{id}")
    @Operation(summary = "Get supply details", description = "Get supply details with specified id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the details of supply")
    })
    @AccessForAdminsAndSellers
    public ResponseEntity<List<SupplyProductResponse>> getSuppliesDetail(@PathVariable("id") Long id) {
        var token = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var authorities = getAuthorities(token.getAuthorities());
        var keycloakId = extractIdFromToken(token);
        List<SupplyProductResponse> result = new ArrayList<>();

        if (authorities.contains(KeycloakRole.SUPER_ADMIN.name())) {
            log.info("Getting supply product details for super-admin");
            result = supplyService.getSuppliesDetail(id);
        } else if (authorities.contains(KeycloakRole.ADMIN.name())) {
            log.info("Getting supply product details for admin");
            result = supplyService.getSuppliesDetail(id, keycloakId);
        } else if (authorities.contains(KeycloakRole.SELLER.name())) {
            log.info("Getting supply product details for seller");
            result = supplyService.getSuppliesDetailOfSeller(id, keycloakId);
        }
        return ResponseEntity.ok(result);
    }


    @GetMapping("/seller")
    @Operation(summary = "Get all supplies of seller", description = "Retrieves a list of supplies within the specified date range")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the list of supplies")
    })
    @SellerAuthorization
    public ResponseEntity<List<SupplySellerResponse>> getSuppliesSeller(@RequestParam("start-date") LocalDate startDate,
                                                                        @RequestParam("end-date") LocalDate endDate) {
        var token = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var id = extractIdFromToken(token);
        List<SupplySellerResponse> response = supplyService.getSuppliesOfSeller(id, startDate, endDate);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/seller/supply-state/{supplyId}")
    @Operation(summary = "Get supply report for seller", description = "Retrieves the detailed report of a supply by the supply ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the supply report for seller")
    })
    @SellerAuthorization
    public ResponseEntity<List<SupplyStateResponse>> getSupplySellerState(@PathVariable Long supplyId) {
        var token = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var keycloakId = extractIdFromToken(token);
        List<SupplyStateResponse> response = supplyService.getSupplySellerState(supplyId, keycloakId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/seller/report/{supplyId}")
    @SellerAuthorization
    public ResponseEntity<SellerSupplyReport> getSupplySellerReport(@PathVariable Long supplyId) {
        var token = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var keycloakId = extractIdFromToken(token);

        SellerSupplyReport sellerSupplyReport = supplyService.getSupplySellerReport(supplyId, keycloakId);

        return ResponseEntity.ok(sellerSupplyReport);
    }

    @GetMapping("/get-by-box/{boxVendorCode}")
    @Operation(summary = "Get supply by the box", description = "Retrieves the products stored in a supply box identified by the box vendor code. This endpoint checks the authenticated user's credentials and roles, including super admin privileges, to fetch the appropriate supply data based on the user's keycloak ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the supply by the box")
    })
    @AccessForAdminsAndEmployee
    public ResponseEntity<ProductStorageResponse> getSupplyByBox(@PathVariable String boxVendorCode) {
        var token = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var keycloakId = extractIdFromToken(token);
        var isSuperAdmin = Utils.getAuthorities(token.getAuthorities()).contains(KeycloakRole.SUPER_ADMIN.name());

        var result = supplyService.getSuppliesProducts(keycloakId, boxVendorCode, isSuperAdmin);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/employee")
    @Operation(summary = "Get all supplies of employee", description = "Retrieves a list of supplies of employee within the specified date range")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list of supplies")
    })
    @StoreEmployeeAuthorization
    public ResponseEntity<List<SupplyStorageResponse>> getSuppliesEmployee(@RequestParam("start-date") LocalDate startDate,
                                                                           @RequestParam("end-date") LocalDate endDate) {
        var token = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var keycloakId = extractIdFromToken(token);

        var result = supplyService.getSuppliesOfStorage(keycloakId, startDate, endDate);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/employee/products")
    @Operation(summary = "Get employee supply products", description = "Retrieves the products associated with the specified supply ID for the employee")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the employee supply products")
    })
    @StoreEmployeeAuthorization
    public ResponseEntity<ProductStorageResponse> getSuppliesEmployee(@RequestParam("supply-id")
                                                                      Long supplyId) {
        var token = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var keycloakId = extractIdFromToken(token);

        var result = supplyService.getSuppliesProducts(keycloakId, supplyId);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/employee/scan")
    @Operation(summary = "Process supply scan", description = "Processes the supply scan request for the employee")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully processed the supply scan request")
    })
    @StoreEmployeeAuthorization
    public ResponseEntity<Void> processSupply(@RequestBody @Valid SupplyScanRequest supplyScanRequest) {
        var token = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var keycloakId = extractIdFromToken(token);

        supplyService.processSupplyByEmployee(keycloakId, supplyScanRequest);

        return ResponseEntity.status(HttpStatus.CREATED.value()).build();
    }
}
