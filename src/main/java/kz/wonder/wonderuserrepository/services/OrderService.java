package kz.wonder.wonderuserrepository.services;

import kz.wonder.wonderuserrepository.dto.response.EmployeeOrderResponse;
import kz.wonder.wonderuserrepository.dto.response.OrderDetailResponse;
import kz.wonder.wonderuserrepository.dto.response.OrderEmployeeDetailResponse;
import kz.wonder.wonderuserrepository.dto.response.OrderResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.List;

public interface OrderService {
    Page<OrderResponse> getSellerOrdersByKeycloakId(String keycloakId, LocalDate startDate, LocalDate endDate, PageRequest pageRequest);

    Page<OrderResponse> getAdminOrdersByKeycloakId(String keycloakId, LocalDate startDate, LocalDate endDate, PageRequest pageRequest);

    List<EmployeeOrderResponse> getEmployeeOrders(String keycloakId, LocalDate startDate, LocalDate endDate);

    void updateOrders();

    List<OrderDetailResponse> getAdminOrderDetails(String keycloakId, String orderCode);

    List<OrderDetailResponse> getSellerOrderDetails(String keycloakId, String orderCode);

    OrderEmployeeDetailResponse getEmployeeOrderDetails(String keycloakId, String orderCode);
}
