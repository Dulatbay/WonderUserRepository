package kz.wonder.wonderuserrepository.controllers;

import jakarta.validation.Valid;
import kz.wonder.wonderuserrepository.constants.Utils;
import kz.wonder.wonderuserrepository.dto.request.AssembleProductRequest;
import kz.wonder.wonderuserrepository.dto.response.AssembleProcessResponse;
import kz.wonder.wonderuserrepository.security.authorizations.base.StoreEmployeeAuthorization;
import kz.wonder.wonderuserrepository.services.AssemblyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@StoreEmployeeAuthorization
@RequestMapping("/assemblies")
public class AssemblyController {
    private final AssemblyService assemblyService;


//    @GetMapping
//    @Operation(summary = "Get current assemblies", description = "Returns a paginated list of current assemblies based on the provided search parameters")
//    @ApiResponses(value = {
//            @ApiResponse(responseCode = "200", description = "Successfully retrieved assemblies",
//                    content = @Content(schema = @Schema(implementation = PaginatedResponse.class))),
//            @ApiResponse(responseCode = "400", description = "Invalid input parameters"),
//            @ApiResponse(responseCode = "401", description = "Unauthorized access"),
//            @ApiResponse(responseCode = "500", description = "Internal server error")
//    })
//    public ResponseEntity<PaginatedResponse<EmployeeAssemblyResponse>> getCurrentAssemblies(@RequestParam("orderCreationStartDate") LocalDate orderCreationStartDate,
//                                                                                            @RequestParam("orderCreationEndDate") LocalDate orderCreationEndDate,
//                                                                                            @RequestParam(defaultValue = "0") int page,
//                                                                                            @RequestParam(defaultValue = "10") int size,
//                                                                                            @RequestParam(name = "deliveryMode", required = false) DeliveryMode deliveryMode,
//                                                                                            @RequestParam(name = "productStateInStore", required = false) ProductStateInStore productStateInStore,
//                                                                                            @RequestParam(name = "sortBy", defaultValue = "id") String sortBy) {
//        var token = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
//        var keycloakId = Utils.extractIdFromToken(token);
//
//        Page<EmployeeAssemblyResponse> assemblyResponse = assemblyService.findAssembliesByParams(keycloakId, new AssemblySearchParameters(orderCreationStartDate, orderCreationEndDate, page, size, deliveryMode, sortBy, productStateInStore));
//
//        return ResponseEntity.ok(new PaginatedResponse<>(assemblyResponse));
//    }

    @PostMapping("/{orderCode}/finish")
    public ResponseEntity<Void> finishAssembly(@PathVariable("orderCode") String orderCode) {
        var token = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var keycloakId = Utils.extractIdFromToken(token);

        assemblyService.finishAssemble(orderCode, keycloakId);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{orderCode}/start")
    public ResponseEntity<AssembleProcessResponse> startAssemble(@PathVariable String orderCode) {
        var token = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();

        AssembleProcessResponse assembleProcessResponse = assemblyService.startAssemble(token, orderCode);

        return ResponseEntity.ok(assembleProcessResponse);
    }

//    @GetMapping("/{orderCode}")
//    @Operation(summary = "Get assembly by order code", description = "This endpoint returns the assemblies based on the provided order code")
//    @ApiResponses(value = {
//            @ApiResponse(responseCode = "200", description = "Successfully retrieved assemblies based on the order code")
//    })
//    public ResponseEntity<AssembleProcessResponse> getAssembly(@PathVariable String orderCode) {
//        var token = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
//
//        AssembleProcessResponse assembleProcessResponse = assemblyService.getAssemble(token, orderCode);
//
//        return ResponseEntity.ok(assembleProcessResponse);
//
//    }

    @PostMapping("/{orderCode}/assemble-product")
    public ResponseEntity<AssembleProcessResponse> assembleProduct(@RequestBody @Valid AssembleProductRequest assembleProductRequest, @PathVariable String orderCode) {
        var token = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();

        AssembleProcessResponse assembleProductResponse = assemblyService.assembleProduct(token, assembleProductRequest, orderCode);

        return ResponseEntity.ok(assembleProductResponse);
    }

}
