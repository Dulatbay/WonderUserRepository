package kz.wonder.wonderuserrepository.services;

import kz.wonder.wonderuserrepository.dto.response.EmployeeOrderResponse;
import kz.wonder.wonderuserrepository.dto.response.OrderDetailResponse;
import kz.wonder.wonderuserrepository.dto.response.OrderEmployeeDetailResponse;
import kz.wonder.wonderuserrepository.dto.response.OrderResponse;
import kz.wonder.wonderuserrepository.entities.KaspiToken;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.List;

public interface OrderService {
    Page<OrderResponse> getSellerOrdersByKeycloakId(String keycloakId, LocalDate startDate, LocalDate endDate, PageRequest pageRequest);

    Page<OrderResponse> getAdminOrdersByKeycloakId(String keycloakId, LocalDate startDate, LocalDate endDate, PageRequest pageRequest);

    void processTokenOrders(KaspiToken token, long startDate, long currentTime, int pageNumber);

    List<EmployeeOrderResponse> getEmployeeOrders(String keycloakId, LocalDate startDate, LocalDate endDate);

    List<OrderDetailResponse> getAdminOrderDetails(String keycloakId, String orderId);

    List<OrderDetailResponse> getSellerOrderDetails(String keycloakId, String orderId);

    List<OrderEmployeeDetailResponse> getEmployeeOrderDetails(String keycloakId, String orderId);
}
