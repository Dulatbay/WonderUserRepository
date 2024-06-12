package kz.wonder.wonderuserrepository.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import kz.wonder.wonderuserrepository.dto.response.CityResponse;
import kz.wonder.wonderuserrepository.security.authorizations.base.SuperAdminAuthorization;
import kz.wonder.wonderuserrepository.services.CityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/cities")
public class CityController {
    private final CityService cityService;

    @GetMapping()
    @Operation(summary = "Get all cities", description = "This endpoint returns the list of cities")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved all cities")
    })
    public ResponseEntity<List<CityResponse>> getCities() {
        return ResponseEntity.ok(cityService.getAllCities());
    }

    @PostMapping("/sync")
    @Operation(summary = "Synchronize with Kaspi", description = "This endpoint synchronizes the local city data with the data from Kaspi")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Successfully synchronized with Kaspi")
    })
    @SuperAdminAuthorization
    public ResponseEntity<Void> sync() {
        cityService.syncWithKaspi();
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
