package kz.wonder.wonderuserrepository.mappers;

import kz.wonder.kaspi.client.model.Order.OrderEntry;
import kz.wonder.wonderuserrepository.dto.response.EmployeeOrderResponse;
import kz.wonder.wonderuserrepository.dto.response.OrderDetailResponse;
import kz.wonder.wonderuserrepository.dto.response.OrderEmployeeDetailResponse;
import kz.wonder.wonderuserrepository.dto.response.OrderResponse;
import kz.wonder.wonderuserrepository.entities.*;
import kz.wonder.wonderuserrepository.repositories.StoreCellProductRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static kz.wonder.wonderuserrepository.constants.Utils.getLocalDateTimeFromTimestamp;
import static kz.wonder.wonderuserrepository.constants.ValueConstants.ZONE_ID;

@Component
public class OrderMapper {
    private static StoreCellProductRepository storeCellProductRepository;

    public static OrderResponse mapToOrderResponse(KaspiOrder kaspiOrder, Double tradePrice) {
        return OrderResponse.builder()
                .code(kaspiOrder.getCode())
                .tradePrice(tradePrice)
                .kaspiId(kaspiOrder.getKaspiId())
                .totalPrice(kaspiOrder.getTotalPrice())
                .paymentMode(kaspiOrder.getPaymentMode())
                .state(kaspiOrder.getStatus())
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

    public static EmployeeOrderResponse mapToEmployeeOrderResponse(KaspiOrder kaspiOrder) {
        EmployeeOrderResponse orderResponse = new EmployeeOrderResponse();

        orderResponse.setOrderCode(kaspiOrder.getCode());
        orderResponse.setOrderCreatedAt(Instant.ofEpochMilli(kaspiOrder.getCreationDate()).atZone(ZONE_ID).toLocalDateTime());
        orderResponse.setOrderStatus(kaspiOrder.getStatus());
        orderResponse.setOrderToSendTime(getLocalDateTimeFromTimestamp(kaspiOrder.getCourierTransmissionPlanningDate()));
        orderResponse.setDeliveryType(kaspiOrder.getDeliveryMode());

        return orderResponse;
    }

    public static OrderDetailResponse toOrderDetailResponse(KaspiOrderProduct kaspiOrderProduct, KaspiOrder kaspiOrder) {
        var product = kaspiOrderProduct.getProduct();
        var supplyBoxProduct = kaspiOrderProduct.getSupplyBoxProduct();
        var storeCellProductOptional = storeCellProductRepository.findBySupplyBoxProductId(supplyBoxProduct.getId());

        OrderDetailResponse orderDetailResponse = new OrderDetailResponse();
        orderDetailResponse.setProductName(product.getName());
        orderDetailResponse.setProductArticle(supplyBoxProduct.getArticle());
        orderDetailResponse.setCellCode(storeCellProductOptional.isPresent() ? storeCellProductOptional.get().getStoreCell().getCode() : "Not accepted yet");
        orderDetailResponse.setProductVendorCode(product.getVendorCode());
        orderDetailResponse.setProductTradePrice(product.getTradePrice());
        orderDetailResponse.setProductSellPrice(kaspiOrder.getTotalPrice()); // todo: тут прибыль от заказа, как достать прибыль именно от одного продукта?(посмотреть потом в апи)
        orderDetailResponse.setIncome(orderDetailResponse.getProductSellPrice() - orderDetailResponse.getProductTradePrice());
        return orderDetailResponse;
    }

    public static OrderEmployeeDetailResponse toOrderEmployeeDetailResponse(KaspiOrder order, List<OrderEmployeeDetailResponse.Product> orderProducts) {
        OrderEmployeeDetailResponse orderEmployeeDetailResponse = new OrderEmployeeDetailResponse();
        orderEmployeeDetailResponse.setProducts(orderProducts);
        orderEmployeeDetailResponse.setDeliveryMode(order.getDeliveryMode());
        orderEmployeeDetailResponse.setDeliveryTime(getLocalDateTimeFromTimestamp(order.getPlannedDeliveryDate()));
        return orderEmployeeDetailResponse;
    }

    public static OrderEmployeeDetailResponse.Product mapToGetOrderEmployeeProduct(Optional<Product> product,
                                                                                   Optional<SupplyBoxProduct> supplyBoxProductOptional,
                                                                                   Optional<StoreCellProduct> storeCellProductOptional) {
        OrderEmployeeDetailResponse.Product orderProduct = new OrderEmployeeDetailResponse.Product();
        orderProduct.setProductName(product.isEmpty() ? "N/A" : product.get().getName());
        orderProduct.setProductArticle(supplyBoxProductOptional.isEmpty() ? "N/A" : supplyBoxProductOptional.get().getArticle());
        orderProduct.setProductCell(storeCellProductOptional.isPresent() ? storeCellProductOptional.get().getStoreCell().getCode() : "N/A");
        orderProduct.setProductVendorCode(product.isEmpty() ? "N/A" : product.get().getVendorCode());
        return orderProduct;
    }

    public static String extractVendorCode(OrderEntry orderEntry) {
        var vendorCode = orderEntry.getAttributes().getOffer().getCode();
        if (vendorCode != null && !vendorCode.isBlank()) {
            return vendorCode.split("_")[0];
        }
        return null;
    }

    public static void updateKaspiOrderProduct(KaspiOrderProduct existingOrderProduct, KaspiOrder kaspiOrder, Product product, OrderEntry orderEntry, SupplyBoxProduct supplyBoxProductToSave) {
        existingOrderProduct.setOrder(kaspiOrder);
        existingOrderProduct.setProduct(product);
        existingOrderProduct.setQuantity(orderEntry.getAttributes().getQuantity());
        existingOrderProduct.setSupplyBoxProduct(supplyBoxProductToSave);
    }
}
