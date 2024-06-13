package kz.wonder.wonderuserrepository.mappers;

import kz.wonder.kaspi.client.model.Order.OrderEntry;
import kz.wonder.kaspi.client.model.OrdersDataResponse;
import kz.wonder.wonderuserrepository.dto.response.EmployeeOrderResponse;
import kz.wonder.wonderuserrepository.dto.response.OrderDetailResponse;
import kz.wonder.wonderuserrepository.dto.response.OrderEmployeeDetailResponse;
import kz.wonder.wonderuserrepository.dto.response.OrderResponse;
import kz.wonder.wonderuserrepository.entities.*;
import kz.wonder.wonderuserrepository.exceptions.DbObjectNotFoundException;
import kz.wonder.wonderuserrepository.repositories.KaspiCityRepository;
import kz.wonder.wonderuserrepository.repositories.KaspiStoreRepository;
import kz.wonder.wonderuserrepository.repositories.StoreCellProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static kz.wonder.wonderuserrepository.constants.Utils.getLocalDateTimeFromTimestamp;
import static kz.wonder.wonderuserrepository.constants.ValueConstants.ZONE_ID;

@Component
@RequiredArgsConstructor
@Slf4j
public class KaspiOrderMapper {
    private final KaspiDeliveryAddressMapper deliveryAddressMapper;
    private final KaspiStoreMapper kaspiStoreMapper;
    private final KaspiCityRepository kaspiCityRepository;
    private final KaspiStoreRepository kaspiStoreRepository;
    private final StoreCellProductRepository storeCellProductRepository;


    public KaspiOrder toKaspiOrder(KaspiToken token, OrdersDataResponse.OrdersDataItem order, OrdersDataResponse.OrderAttributes orderAttributes) {
        KaspiOrder kaspiOrder = new KaspiOrder();
        kaspiOrder.setKaspiId(order.getOrderId());
        kaspiOrder.setCode(orderAttributes.getCode());
        kaspiOrder.setTotalPrice(orderAttributes.getTotalPrice());
        kaspiOrder.setPaymentMode(orderAttributes.getPaymentMode());

        if (orderAttributes.getDeliveryAddress() != null) {
            kaspiOrder.setDeliveryAddress(deliveryAddressMapper.getKaspiDeliveryAddress(orderAttributes));
        }

        // if the originAddress is null, then an order delivery type is express
        if (orderAttributes.getOriginAddress() != null) {
            var kaspiCity = kaspiCityRepository.findByCode(orderAttributes.getOriginAddress().getCity().getCode())
                    .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND, "Kaspi city not found", ""));

            var kaspiStore = kaspiStoreMapper.getKaspiStore(orderAttributes, orderAttributes.getOriginAddress(), kaspiCity);

            kaspiOrder.setKaspiStore(kaspiStore);
            kaspiOrder.setKaspiCity(kaspiCity);
            kaspiOrder.setWaybill(orderAttributes.getKaspiDelivery().getWaybill());
            kaspiOrder.setCourierTransmissionDate(orderAttributes.getKaspiDelivery().getCourierTransmissionDate());
            kaspiOrder.setCourierTransmissionPlanningDate(orderAttributes.getKaspiDelivery().getCourierTransmissionPlanningDate());
            kaspiOrder.setWaybillNumber(orderAttributes.getKaspiDelivery().getWaybillNumber());
            kaspiOrder.setExpress(orderAttributes.getKaspiDelivery().getExpress());
            kaspiOrder.setReturnedToWarehouse(orderAttributes.getKaspiDelivery().getReturnedToWarehouse());
            kaspiOrder.setFirstMileCourier(orderAttributes.getKaspiDelivery().getFirstMileCourier());
        } else {
            var pickupPointId = orderAttributes.getPickupPointId();

            var kaspiStoreOptional = kaspiStoreRepository.findByPickupPointIdAndWonderUserKeycloakIdAndDeletedIsFalse(pickupPointId, token.getWonderUser().getKeycloakId());

            if (kaspiStoreOptional.isPresent()) {
                kaspiOrder.setKaspiStore(kaspiStoreOptional.get());
                kaspiOrder.setKaspiCity(kaspiStoreOptional.get().getKaspiCity());
            }
        }


        kaspiOrder.setCreditTerm(orderAttributes.getCreditTerm());
        kaspiOrder.setPlannedDeliveryDate(orderAttributes.getPlannedDeliveryDate());
        kaspiOrder.setCreationDate(orderAttributes.getCreationDate());
        kaspiOrder.setDeliveryCostForSeller(orderAttributes.getDeliveryCostForSeller());
        kaspiOrder.setIsKaspiDelivery(orderAttributes.getIsKaspiDelivery());
        kaspiOrder.setDeliveryMode(DeliveryMode.buildDeliveryMode(orderAttributes.getDeliveryMode(), orderAttributes.getIsKaspiDelivery()));
        kaspiOrder.setSignatureRequired(orderAttributes.getSignatureRequired());
        kaspiOrder.setPreOrder(orderAttributes.getPreOrder());
        kaspiOrder.setPickupPointId(orderAttributes.getPickupPointId());
        kaspiOrder.setState(orderAttributes.getState());
        kaspiOrder.setAssembled(orderAttributes.getAssembled());
        kaspiOrder.setApprovedByBankDate(orderAttributes.getApprovedByBankDate());
        kaspiOrder.setStatus(orderAttributes.getStatus());
        kaspiOrder.setCustomerName(orderAttributes.getCustomer().getName());
        kaspiOrder.setCustomerCellPhone(orderAttributes.getCustomer().getCellPhone());
        kaspiOrder.setCustomerFirstName(orderAttributes.getCustomer().getFirstName());
        kaspiOrder.setCustomerLastName(orderAttributes.getCustomer().getLastName());
        kaspiOrder.setDeliveryCost(orderAttributes.getDeliveryCost());
        kaspiOrder.setWonderUser(token.getWonderUser());

        return kaspiOrder;
    }

    public OrderResponse mapToOrderResponse(KaspiOrder kaspiOrder, Double tradePrice) {
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
                .storeFormattedAddress(kaspiOrder.getKaspiStore().getFormattedAddress())
                .build();
    }

    public EmployeeOrderResponse mapToEmployeeOrderResponse(KaspiOrder kaspiOrder) {
        EmployeeOrderResponse orderResponse = new EmployeeOrderResponse();

        orderResponse.setOrderCode(kaspiOrder.getCode());
        orderResponse.setOrderCreatedAt(Instant.ofEpochMilli(kaspiOrder.getCreationDate()).atZone(ZONE_ID).toLocalDateTime());
        orderResponse.setOrderStatus(kaspiOrder.getStatus());
        orderResponse.setOrderToSendTime(getLocalDateTimeFromTimestamp(kaspiOrder.getCourierTransmissionPlanningDate()));
        orderResponse.setDeliveryType(kaspiOrder.getDeliveryMode());

        return orderResponse;
    }

    public OrderDetailResponse toOrderDetailResponse(KaspiOrderProduct kaspiOrderProduct, KaspiOrder kaspiOrder) {
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

    public OrderEmployeeDetailResponse toOrderEmployeeDetailResponse(KaspiOrder order, List<OrderEmployeeDetailResponse.Product> orderProducts) {
        OrderEmployeeDetailResponse orderEmployeeDetailResponse = new OrderEmployeeDetailResponse();
        orderEmployeeDetailResponse.setProducts(orderProducts);
        orderEmployeeDetailResponse.setDeliveryMode(order.getDeliveryMode());
        orderEmployeeDetailResponse.setDeliveryTime(getLocalDateTimeFromTimestamp(order.getPlannedDeliveryDate()));
        return orderEmployeeDetailResponse;
    }

    public OrderEmployeeDetailResponse.Product mapToGetOrderEmployeeProduct(Optional<Product> product,
                                                                            Optional<SupplyBoxProduct> supplyBoxProductOptional,
                                                                            Optional<StoreCellProduct> storeCellProductOptional) {
        OrderEmployeeDetailResponse.Product orderProduct = new OrderEmployeeDetailResponse.Product();
        orderProduct.setProductName(product.isEmpty() ? "N/A" : product.get().getName());
        orderProduct.setProductArticle(supplyBoxProductOptional.isEmpty() ? "N/A" : supplyBoxProductOptional.get().getArticle());
        orderProduct.setProductCell(storeCellProductOptional.isPresent() ? storeCellProductOptional.get().getStoreCell().getCode() : "N/A");
        orderProduct.setProductVendorCode(product.isEmpty() ? "N/A" : product.get().getVendorCode());
        return orderProduct;
    }

    public String extractVendorCode(OrderEntry orderEntry) {
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
