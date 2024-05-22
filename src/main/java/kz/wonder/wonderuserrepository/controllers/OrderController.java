package kz.wonder.wonderuserrepository.controllers;

import io.swagger.v3.oas.annotations.Operation;
import kz.wonder.wonderuserrepository.dto.PaginatedResponse;
import kz.wonder.wonderuserrepository.dto.response.EmployeeOrderResponse;
import kz.wonder.wonderuserrepository.dto.response.OrderDetailResponse;
import kz.wonder.wonderuserrepository.dto.response.OrderEmployeeDetailResponse;
import kz.wonder.wonderuserrepository.dto.response.OrderResponse;
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
    @Operation(summary = "Get seller's order", description = "Retrieves the list of orders for the seller within the specified date range.")
    public ResponseEntity<PaginatedResponse<OrderResponse>> getSellerOrders(@RequestParam("start-date") LocalDate startDate,
                                                                            @RequestParam("end-date") LocalDate endDate,
                                                                            @RequestParam(defaultValue = "0") int page,
                                                                            @RequestParam(defaultValue = "10") int size) {
        var token = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var keycloakId = extractIdFromToken(token);

        PageRequest pageRequest = PageRequest.of(page, size);

        Page<OrderResponse> sellerOrderResponseList = orderService.getSellerOrdersByKeycloakId(keycloakId, startDate, endDate, pageRequest);

        return ResponseEntity.ok().body(new PaginatedResponse<>(sellerOrderResponseList));
    }

    @GetMapping("/admin")
    @Operation(summary = "Get admin order", description = "Retrieves the list of orders for the admin within the specified date range.")
    public ResponseEntity<PaginatedResponse<OrderResponse>> getAdminOrders(@RequestParam("start-date") LocalDate startDate,
                                                              @RequestParam("end-date") LocalDate endDate,
                                                              @RequestParam(defaultValue = "0") int page,
                                                              @RequestParam(defaultValue = "10") int size) {
        var token = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var keycloakId = extractIdFromToken(token);

        PageRequest pageRequest = PageRequest.of(page, size);

        Page<OrderResponse> orderResponseList = orderService.getAdminOrdersByKeycloakId(keycloakId, startDate, endDate, pageRequest);

        return ResponseEntity.ok().body(new PaginatedResponse<>(orderResponseList));
    }

    @GetMapping("/employee")
    @Operation(summary = "Get employee orders", description = "Retrieves the list of employee orders within the specified date range.")
    public ResponseEntity<List<EmployeeOrderResponse>> getEmployeeOrders(@RequestParam("start-date") LocalDate startDate,
                                                                         @RequestParam("end-date") LocalDate endDate) {
        var token = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var keycloakId = extractIdFromToken(token);

        List<EmployeeOrderResponse> orders = orderService.getEmployeeOrders(keycloakId, startDate, endDate);

        return ResponseEntity.ok().body(orders);
    }

    @GetMapping("/admin/details/{orderCode}")
    public ResponseEntity<List<OrderDetailResponse>> getAdminOrderDetails(@PathVariable("orderCode") String orderCode) {
        var token = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var keycloakId = extractIdFromToken(token);
        List<OrderDetailResponse> orderResponse = orderService.getAdminOrderDetails(keycloakId, orderCode);
        return ResponseEntity.ok().body(orderResponse);
    }

    @GetMapping("/seller/details/{orderCode}")
    @Operation(summary = "Get order details for seller", description = "Retrieves the order data for the seller by Order Code")
    public ResponseEntity<List<OrderDetailResponse>> getSellerOrderDetails(@PathVariable("orderCode") String orderCode) {
        var token = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var keycloakId = extractIdFromToken(token);


        List<OrderDetailResponse> orderResponse = orderService.getSellerOrderDetails(keycloakId, orderCode);
        return ResponseEntity.ok().body(orderResponse);
    }

    @GetMapping("/employee/details/{orderCode}")
    @Operation(summary = "Get order details for employee", description = "Retrieves the order data for the employee by Order Code")
    public ResponseEntity<OrderEmployeeDetailResponse> getEmployeeOrderDetails(@PathVariable("orderCode") String orderCode) {
        var token = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var keycloakId = extractIdFromToken(token);


        OrderEmployeeDetailResponse employeeOrderDetails = orderService.getEmployeeOrderDetails(keycloakId, orderCode);
        return ResponseEntity.ok().body(employeeOrderDetails);
    }

}
