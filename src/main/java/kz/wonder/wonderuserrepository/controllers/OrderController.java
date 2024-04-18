package kz.wonder.wonderuserrepository.controllers;

import kz.wonder.wonderuserrepository.dto.response.OrderResponse;
import kz.wonder.wonderuserrepository.services.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static kz.wonder.wonderuserrepository.constants.Utils.extractIdFromToken;

@RequiredArgsConstructor
@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    @GetMapping("/seller")
    public ResponseEntity<List<OrderResponse>> getSellerOrders(){
        var token = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var keycloakId = extractIdFromToken(token);

        List<OrderResponse> sellerOrderResponseList = orderService.getSellerOrdersByKeycloakId(keycloakId);

        return ResponseEntity.ok().body(sellerOrderResponseList);
    }

    @GetMapping("/admin")
    public ResponseEntity<List<OrderResponse>> getAdminOrders(){
        var token = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        var keycloakId = extractIdFromToken(token);

        List<OrderResponse> orderResponseList = orderService.getAdminOrdersByKeycloakId(keycloakId);

        return ResponseEntity.ok().body(orderResponseList);
    }
}
