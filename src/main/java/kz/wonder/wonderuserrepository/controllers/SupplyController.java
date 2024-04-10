package kz.wonder.wonderuserrepository.controllers;

import kz.wonder.wonderuserrepository.dto.request.SupplyCreateRequest;
import kz.wonder.wonderuserrepository.dto.response.*;
import kz.wonder.wonderuserrepository.security.keycloak.KeycloakRole;
import kz.wonder.wonderuserrepository.services.KeycloakService;
import kz.wonder.wonderuserrepository.services.SupplyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static kz.wonder.wonderuserrepository.constants.Utils.extractIdFromToken;
import static kz.wonder.wonderuserrepository.constants.Utils.getAuthorities;

@RestController
@RequiredArgsConstructor
@RequestMapping("/supplies")
public class SupplyController {

    private final SupplyService supplyService;
    private final KeycloakService keycloakService;

    @PostMapping(value = "/process-file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<SupplyProcessFileResponse>> processFile(@RequestPart("file") MultipartFile file) {
        var token = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var userId = extractIdFromToken(token);
        var result = supplyService.processFile(file, userId);
        return ResponseEntity.ok(result);
    }

    @PostMapping
    public ResponseEntity<Map<String, Long>> createSupply(@RequestBody SupplyCreateRequest createRequest) {
        var token = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var userId = extractIdFromToken(token);
        long id = supplyService.createSupply(createRequest, userId);
        Map<String, Long> res = new HashMap<>();
        res.put("id", id);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    @GetMapping("/admin")
    public ResponseEntity<List<SupplyAdminResponse>> getSuppliesAdmin(@RequestParam("start-date") LocalDate startDate,
                                                                      @RequestParam("end-date") LocalDate endDate
    ) {
        var token = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var id = extractIdFromToken(token);
        var userRepresentation = keycloakService.getUserById(id).toRepresentation();

        List<SupplyAdminResponse> result = supplyService.getSuppliesOfAdmin(startDate, endDate, userRepresentation.getId(), userRepresentation.getFirstName() + " " + userRepresentation.getLastName());
        return ResponseEntity.ok(result);
    }


    @GetMapping("/detail/{id}")
    public ResponseEntity<List<SupplyProductResponse>> getSuppliesDetail(@PathVariable("id") Long id) {
        var token = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var authorities = getAuthorities(token.getAuthorities());
        var keycloakId = extractIdFromToken(token);
        List<SupplyProductResponse> result = new ArrayList<>();

        if (authorities.contains(KeycloakRole.SUPER_ADMIN.name()))
            result = supplyService.getSuppliesDetail(id);
        else if (authorities.contains(KeycloakRole.ADMIN.name()))
            result = supplyService.getSuppliesDetail(id, keycloakId);
        else if (authorities.contains(KeycloakRole.SELLER.name()))
            result = supplyService.getSuppliesDetailOfSeller(id, keycloakId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/seller")
    public ResponseEntity<List<SupplySellerResponse>> getSuppliesSeller(@RequestParam("start-date") LocalDate startDate,
                                                                        @RequestParam("end-date") LocalDate endDate) {
        var token = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var id = extractIdFromToken(token);
        List<SupplySellerResponse> response = supplyService.getSuppliesOfSeller(id, startDate, endDate);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/seller/report/{supplyId}")
    public ResponseEntity<List<SupplyReportResponse>> getSupplyReportSeller(@PathVariable Long supplyId) {
        var token = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var keycloakId = extractIdFromToken(token);
        List<SupplyReportResponse> response = supplyService.getSupplyReport(supplyId, keycloakId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/employee")
    public ResponseEntity<List<SupplyStorageResponse>> getSuppliesEmployee(@RequestParam("start-date") LocalDate startDate,
                                                                           @RequestParam("end-date") LocalDate endDate) {
        var token = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var keycloakId = extractIdFromToken(token);

        var result = supplyService.getSuppliesOfStorage(keycloakId, startDate, endDate);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/employee/products")
    public ResponseEntity<ProductStorageResponse> getSuppliesEmployee(@RequestParam("supply-id")
                                                                      Long supplyId) {
        var token = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var keycloakId = extractIdFromToken(token);

        var result = supplyService.getSuppliesProducts(keycloakId, supplyId);
        return ResponseEntity.ok(result);
    }


}
