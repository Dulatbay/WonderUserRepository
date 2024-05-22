package kz.wonder.wonderuserrepository.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import kz.wonder.wonderuserrepository.constants.Utils;
import kz.wonder.wonderuserrepository.dto.PaginatedResponse;
import kz.wonder.wonderuserrepository.dto.enums.AssemblyMode;
import kz.wonder.wonderuserrepository.dto.enums.DeliveryMode;
import kz.wonder.wonderuserrepository.dto.params.AssemblySearchParameters;
import kz.wonder.wonderuserrepository.dto.response.EmployeeAssemblyResponse;
import kz.wonder.wonderuserrepository.services.AssemblyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/employee/assemblies")
public class AssemblyController {
    private final AssemblyService assemblyService;


    @GetMapping
    @Operation(summary = "Get Current Assemblies", description = "Returns a paginated list of current assemblies based on the provided search parameters")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved assemblies",
                    content = @Content(schema = @Schema(implementation = PaginatedResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input parameters"),
            @ApiResponse(responseCode = "401", description = "Unauthorized access"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<PaginatedResponse<EmployeeAssemblyResponse>> getCurrentAssemblies(@RequestParam("start-date") LocalDate startDate,
                                                                                            @RequestParam("end-date") LocalDate endDate,
                                                                                            @RequestParam(defaultValue = "0") int page,
                                                                                            @RequestParam(defaultValue = "10") int size,
                                                                                            @RequestParam(name = "deliveryMode", required = false) DeliveryMode deliveryMode,
                                                                                            @RequestParam(name = "assemblyMode", required = false) AssemblyMode assemblyMode,
                                                                                            @RequestParam(name = "sortBy", required = false, defaultValue = "id") String sortBy) {
        var token = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var keycloakId = Utils.extractIdFromToken(token);

        Page<EmployeeAssemblyResponse> assemblyResponse = assemblyService.findAssembliesByParams(keycloakId, new AssemblySearchParameters(startDate, endDate, page, size, deliveryMode, assemblyMode, sortBy));

        return ResponseEntity.ok(new PaginatedResponse<>(assemblyResponse));
    }
}
