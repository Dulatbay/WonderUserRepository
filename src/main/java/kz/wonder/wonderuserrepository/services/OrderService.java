package kz.wonder.wonderuserrepository.services;

import kz.wonder.kaspi.client.model.OrdersDataResponse;
import kz.wonder.kaspi.client.model.response.Order.OrderEntry;
import kz.wonder.wonderuserrepository.dto.params.OrderSearchParams;
import kz.wonder.wonderuserrepository.dto.response.EmployeeOrderResponse;
import kz.wonder.wonderuserrepository.dto.response.OrderDetailResponse;
import kz.wonder.wonderuserrepository.dto.response.OrderEmployeeDetailResponse;
import kz.wonder.wonderuserrepository.dto.response.OrderResponse;
import kz.wonder.wonderuserrepository.entities.KaspiToken;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

public interface OrderService {
    Page<OrderResponse> getSellerOrdersByKeycloakId(String keycloakId, LocalDate startDate, LocalDate endDate, OrderSearchParams orderSearchParams, PageRequest pageRequest);

    Page<OrderResponse> getAdminOrdersByKeycloakId(String keycloakId, LocalDate startDate, LocalDate endDate, OrderSearchParams orderSearchParams, PageRequest pageRequest);

    Page<EmployeeOrderResponse> getEmployeeOrders(String keycloakId, LocalDate startDate, LocalDate endDate, OrderSearchParams orderSearchParams, PageRequest pageRequest);

    void updateOrders();

    List<OrderDetailResponse> getAdminOrderDetails(String keycloakId, String orderCode);

    List<OrderDetailResponse> getSellerOrderDetails(String keycloakId, String orderCode);

    OrderEmployeeDetailResponse getEmployeeOrderDetails(String keycloakId, String orderCode);
}
