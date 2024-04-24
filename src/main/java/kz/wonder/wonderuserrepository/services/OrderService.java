package kz.wonder.wonderuserrepository.services;

import kz.wonder.wonderuserrepository.dto.response.EmployeeOrderResponse;
import kz.wonder.wonderuserrepository.dto.response.OrderDetailResponse;
import kz.wonder.wonderuserrepository.dto.response.OrderResponse;
import kz.wonder.wonderuserrepository.entities.KaspiToken;

import java.time.LocalDate;
import java.util.List;

public interface OrderService {
    List<OrderResponse> getSellerOrdersByKeycloakId(String keycloakId, LocalDate startDate, LocalDate endDate);
    List<OrderResponse> getAdminOrdersByKeycloakId(String keycloakId, LocalDate startDate, LocalDate endDate);
    void processTokenOrders(KaspiToken token, long startDate, long currentTime);
    List<EmployeeOrderResponse> getEmployeeOrders(String keycloakId, LocalDate startDate, LocalDate endDate);
    List<OrderDetailResponse> getAdminOrderDetails(String keycloakId, String orderId);
    List<OrderDetailResponse> getSellerOrderDetails(String keycloakId, String orderId);
}
