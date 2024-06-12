package kz.wonder.wonderuserrepository.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import kz.wonder.wonderuserrepository.dto.base.PaginatedResponse;
import kz.wonder.wonderuserrepository.dto.params.OrderSearchParams;
import kz.wonder.wonderuserrepository.dto.response.EmployeeOrderResponse;
import kz.wonder.wonderuserrepository.dto.response.OrderDetailResponse;
import kz.wonder.wonderuserrepository.dto.response.OrderEmployeeDetailResponse;
import kz.wonder.wonderuserrepository.dto.response.OrderResponse;
import kz.wonder.wonderuserrepository.entities.DeliveryMode;
import kz.wonder.wonderuserrepository.security.authorizations.AccessForAdmins;
import kz.wonder.wonderuserrepository.security.authorizations.base.SellerAuthorization;
import kz.wonder.wonderuserrepository.security.authorizations.base.StoreEmployeeAuthorization;
import kz.wonder.wonderuserrepository.services.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

import static kz.wonder.wonderuserrepository.constants.Utils.extractIdFromToken;

@RequiredArgsConstructor
@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    @GetMapping("/seller")
    @Operation(summary = "Get seller orders", description = "Retrieves the list of orders for the seller within the specified date range.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the seller orders")
    })
    @SellerAuthorization
    public ResponseEntity<PaginatedResponse<OrderResponse>> getSellerOrders(@RequestParam("start-date") LocalDate startDate,
                                                                            @RequestParam("end-date") LocalDate endDate,
                                                                            @RequestParam(defaultValue = "0") int page,
                                                                            @RequestParam(defaultValue = "10") int size,
                                                                            @ModelAttribute OrderSearchParams orderSearchParams) {
        var token = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var keycloakId = extractIdFromToken(token);

        PageRequest pageRequest = PageRequest.of(page, size);

        Page<OrderResponse> sellerOrderResponseList = orderService.getSellerOrdersByKeycloakId(keycloakId, startDate, endDate, orderSearchParams, pageRequest);

        return ResponseEntity.ok().body(new PaginatedResponse<>(sellerOrderResponseList));
    }

    @GetMapping("/admin")
    @Operation(summary = "Get admin orders", description = "Retrieves the list of orders for the admin within the specified date range.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the admin orders")
    })
    @AccessForAdmins
    public ResponseEntity<PaginatedResponse<OrderResponse>> getAdminOrders(@RequestParam("start-date") LocalDate startDate,
                                                                           @RequestParam("end-date") LocalDate endDate,
                                                                           @RequestParam(defaultValue = "0") int page,
                                                                           @RequestParam(defaultValue = "10") int size,
                                                                           @ModelAttribute OrderSearchParams orderSearchParams) {
        var token = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var keycloakId = extractIdFromToken(token);

        PageRequest pageRequest = PageRequest.of(page, size);

        Page<OrderResponse> orderResponseList = orderService.getAdminOrdersByKeycloakId(keycloakId, startDate, endDate, orderSearchParams, pageRequest);

        return ResponseEntity.ok().body(new PaginatedResponse<>(orderResponseList));
    }

    @GetMapping("/employee")
    @Operation(summary = "Get employee orders", description = "Retrieves the list of employee orders within the specified date range.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the employee orders")
    })
    @StoreEmployeeAuthorization
    public ResponseEntity<PaginatedResponse<EmployeeOrderResponse>> getEmployeeOrders(@RequestParam("start-date") LocalDate startDate,
                                                                                      @RequestParam("end-date") LocalDate endDate,
                                                                                      @RequestParam(defaultValue = "0") int page,
                                                                                      @RequestParam(defaultValue = "10") int size,
                                                                                      @ModelAttribute OrderSearchParams orderSearchParams) {
        var token = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var keycloakId = extractIdFromToken(token);

        PageRequest pageRequest = PageRequest.of(page, size);


        var orders = orderService.getEmployeeOrders(keycloakId, startDate, endDate, orderSearchParams, pageRequest);

        return ResponseEntity.ok().body(new PaginatedResponse<>(orders));
    }

    @GetMapping("/admin/details/{orderCode}")
    @Operation(summary = "Get order details for admin", description = "Retrieves any order data by Order Code")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the order details for admin")
    })
    @AccessForAdmins
    public ResponseEntity<List<OrderDetailResponse>> getAdminOrderDetails(@PathVariable("orderCode") String orderCode) {
        var token = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var keycloakId = extractIdFromToken(token);
        List<OrderDetailResponse> orderResponse = orderService.getAdminOrderDetails(keycloakId, orderCode);
        return ResponseEntity.ok().body(orderResponse);
    }

    @GetMapping("/seller/details/{orderCode}")
    @Operation(summary = "Get order details for seller", description = "Retrieves the order data for the seller by Order Code")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the order details for seller")
    })
    @SellerAuthorization
    public ResponseEntity<List<OrderDetailResponse>> getSellerOrderDetails(@PathVariable("orderCode") String orderCode) {
        var token = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var keycloakId = extractIdFromToken(token);


        List<OrderDetailResponse> orderResponse = orderService.getSellerOrderDetails(keycloakId, orderCode);
        return ResponseEntity.ok().body(orderResponse);
    }

    @GetMapping("/employee/details/{orderCode}")
    @Operation(summary = "Get order details for employee", description = "Retrieves the order data for the employee by Order Code")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the order details for employee")
    })
    @StoreEmployeeAuthorization
    public ResponseEntity<OrderEmployeeDetailResponse> getEmployeeOrderDetails(@PathVariable("orderCode") String orderCode) {
        var token = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var keycloakId = extractIdFromToken(token);


        OrderEmployeeDetailResponse employeeOrderDetails = orderService.getEmployeeOrderDetails(keycloakId, orderCode);
        return ResponseEntity.ok().body(employeeOrderDetails);
    }

}
