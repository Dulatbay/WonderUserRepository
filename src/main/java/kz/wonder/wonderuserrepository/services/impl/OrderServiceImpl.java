package kz.wonder.wonderuserrepository.services.impl;

import kz.wonder.wonderuserrepository.dto.response.OrderResponse;
import kz.wonder.wonderuserrepository.entities.KaspiOrder;
import kz.wonder.wonderuserrepository.repositories.KaspiOrderRepository;
import kz.wonder.wonderuserrepository.services.OrderService;
import kz.wonder.wonderuserrepository.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final KaspiOrderRepository kaspiOrderRepository;
    private final UserService userService;

    @Override
    public List<OrderResponse> getSellerOrdersByKeycloakId(String keycloakId) {
        var kaspiOrderInDb = kaspiOrderRepository.findAllByWonderUserKeycloakId(keycloakId);

        return kaspiOrderInDb
                .stream()
                .map(OrderServiceImpl::getOrderResponse)
                .toList();
    }

    private static OrderResponse getOrderResponse(KaspiOrder kaspiOrder) {
        return OrderResponse.builder()
                .id(kaspiOrder.getId())
                .code(kaspiOrder.getCode())
                .kaspiId(kaspiOrder.getKaspiId())
                .totalPrice(kaspiOrder.getTotalPrice())
                .paymentMode(kaspiOrder.getPaymentMode())
                .plannedDeliveryDate(kaspiOrder.getPlannedDeliveryDate())
                .creationDate(kaspiOrder.getCreationDate())
                .deliveryCostForSeller(kaspiOrder.getDeliveryCostForSeller())
                .isKaspiDelivery(kaspiOrder.getIsKaspiDelivery())
                .deliveryMode(kaspiOrder.getDeliveryMode())
                .waybill(kaspiOrder.getWaybill())
                .courierTransmissionDate(kaspiOrder.getCourierTransmissionDate())
                .courierTransmissionPlanningDate(kaspiOrder.getCourierTransmissionPlanningDate())
                .waybillNumber(kaspiOrder.getWaybillNumber())
                .deliveryCost(kaspiOrder.getDeliveryCost())
                .sellerName(kaspiOrder.getWonderUser().getKaspiToken().getSellerName())
                .build();
    }

    @Override
    public List<OrderResponse> getAdminOrdersByKeycloakId(String keycloakId) {
        var wonderUser = userService.getUserByKeycloakId(keycloakId);
        var stores = wonderUser.getStores();

        var result = new ArrayList<OrderResponse>();

        stores.forEach(store -> {
            var orders = store.getOrders();
            orders.forEach(order -> {
                result.add(getOrderResponse(order));
            });
        });
        return result;
    }
}
