package kz.wonder.wonderuserrepository.services;

import kz.wonder.wonderuserrepository.dto.response.OrderResponse;
import kz.wonder.wonderuserrepository.entities.KaspiToken;

import java.util.List;

public interface OrderService {
    List<OrderResponse> getSellerOrdersByKeycloakId(String keycloakId);

    List<OrderResponse> getAdminOrdersByKeycloakId(String keycloakId);
    void processTokenOrders(KaspiToken token, long startDate, long currentTime);
}
