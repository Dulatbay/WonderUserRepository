package kz.wonder.wonderuserrepository.mappers;

import kz.wonder.kaspi.client.model.OrdersDataResponse;
import kz.wonder.kaspi.client.model.response.Order.OrderEntry;
import kz.wonder.wonderuserrepository.dto.enums.OrderStateInStore;
import kz.wonder.wonderuserrepository.dto.response.EmployeeOrderResponse;
import kz.wonder.wonderuserrepository.dto.response.OrderDetailResponse;
import kz.wonder.wonderuserrepository.dto.response.OrderEmployeeDetailResponse;
import kz.wonder.wonderuserrepository.dto.response.OrderResponse;
import kz.wonder.wonderuserrepository.entities.*;
import kz.wonder.wonderuserrepository.entities.enums.DeliveryMode;
import kz.wonder.wonderuserrepository.repositories.KaspiOrderRepository;
import kz.wonder.wonderuserrepository.repositories.OrderPackageProcessRepository;
import kz.wonder.wonderuserrepository.repositories.StoreCellProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static kz.wonder.wonderuserrepository.constants.Utils.getLocalDateTimeFromTimestamp;
import static kz.wonder.wonderuserrepository.constants.ValueConstants.ZONE_ID;

@Component
@RequiredArgsConstructor
@Slf4j
public class KaspiOrderMapper {
    private final StoreCellProductRepository storeCellProductRepository;
    private final KaspiOrderRepository kaspiOrderRepository;
    private final BarcodeMapper barcodeMapper;
    private final OrderPackageProcessRepository orderPackageProcessRepository;

    public KaspiOrder saveKaspiOrder(KaspiToken token, OrdersDataResponse.OrdersDataItem order, OrdersDataResponse.OrderAttributes orderAttributes) {
        KaspiOrder kaspiOrder = new KaspiOrder();

        this.mapToKaspiOrder(token, order, orderAttributes, kaspiOrder);

        return kaspiOrder;
    }

    private void mapToKaspiOrder(KaspiToken token, OrdersDataResponse.OrdersDataItem order, OrdersDataResponse.OrderAttributes orderAttributes, KaspiOrder kaspiOrder) {
        kaspiOrder.setKaspiId(order.getOrderId());
        kaspiOrder.setCode(orderAttributes.getCode());
        kaspiOrder.setTotalPrice(orderAttributes.getTotalPrice());
        kaspiOrder.setPaymentMode(orderAttributes.getPaymentMode());
        kaspiOrder.setCreditTerm(orderAttributes.getCreditTerm());
        kaspiOrder.setPlannedDeliveryDate(orderAttributes.getPlannedDeliveryDate());
        kaspiOrder.setCreationDate(orderAttributes.getCreationDate());
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
        kaspiOrder.setDeliveryCostForSeller(orderAttributes.getDeliveryCostForSeller());
        kaspiOrder.setWonderUser(token.getWonderUser());
        kaspiOrder.setWaybill(orderAttributes.getKaspiDelivery().getWaybill());
        kaspiOrder.setCourierTransmissionDate(orderAttributes.getKaspiDelivery().getCourierTransmissionDate());
        kaspiOrder.setCourierTransmissionPlanningDate(orderAttributes.getKaspiDelivery().getCourierTransmissionPlanningDate());
        kaspiOrder.setWaybillNumber(orderAttributes.getKaspiDelivery().getWaybillNumber());
        kaspiOrder.setExpress(orderAttributes.getKaspiDelivery().getExpress());
        kaspiOrder.setReturnedToWarehouse(orderAttributes.getKaspiDelivery().getReturnedToWarehouse());
        kaspiOrder.setFirstMileCourier(orderAttributes.getKaspiDelivery().getFirstMileCourier());
    }

    public KaspiOrder updateKaspiOrder(KaspiOrder kaspiOrder, KaspiToken token, OrdersDataResponse.OrdersDataItem order, OrdersDataResponse.OrderAttributes orderAttributes) {

        mapToKaspiOrder(token, order, orderAttributes, kaspiOrder);

        return kaspiOrderRepository.save(kaspiOrder);
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
        orderResponse.setShopName(kaspiOrder.getWonderUser().getKaspiToken().getSellerName());
        orderResponse.setFormattedAddress(kaspiOrder.getKaspiStore().getFormattedAddress());
        orderResponse.setOrderCreatedAt(Instant.ofEpochMilli(kaspiOrder.getCreationDate()).atZone(ZONE_ID).toLocalDateTime());
        orderResponse.setOrderToSendTime(getLocalDateTimeFromTimestamp(kaspiOrder.getCourierTransmissionPlanningDate()));
        orderResponse.setOrderStatus(OrderStateInStore.getOrderStatus(kaspiOrder));
        orderResponse.setDeliveryType(kaspiOrder.getDeliveryMode());
        orderResponse.setPickUpPerson(kaspiOrder.getFirstMileCourier());
        orderResponse.setPrice(kaspiOrder.getTotalPrice());
        orderResponse.setProductsCount(Optional.ofNullable(kaspiOrder.getProducts()).map(Set::size).orElse(0));

        return orderResponse;
    }


    public OrderDetailResponse toOrderDetailResponse(KaspiOrderProduct kaspiOrderProduct) {
        var product = kaspiOrderProduct.getProduct();
        var supplyBoxProduct = kaspiOrderProduct.getSupplyBoxProduct();
        var storeCellProductOptional = storeCellProductRepository.findBySupplyBoxProductId(supplyBoxProduct.getId());

        OrderDetailResponse orderDetailResponse = new OrderDetailResponse();
        orderDetailResponse.setProductName(product.getName());
        orderDetailResponse.setProductArticle(supplyBoxProduct.getArticle());
        orderDetailResponse.setCellCode(storeCellProductOptional.isPresent() ? storeCellProductOptional.get().getStoreCell().getCode() : "Not accepted yet");
        orderDetailResponse.setPathToBoxBarcode(barcodeMapper.getPathToBoxBarcode(supplyBoxProduct.getSupplyBox()));
        orderDetailResponse.setPathToProductBarcode(barcodeMapper.getPathToProductBarcode(supplyBoxProduct));
        orderDetailResponse.setProductVendorCode(product.getVendorCode());
        orderDetailResponse.setProductTradePrice(product.getTradePrice());
        orderDetailResponse.setProductSellPrice(kaspiOrderProduct.getBasePrice());
        orderDetailResponse.setIncome(orderDetailResponse.getProductSellPrice() - orderDetailResponse.getProductTradePrice());
        return orderDetailResponse;
    }

    public OrderEmployeeDetailResponse toOrderEmployeeDetailResponse(KaspiOrder order, List<OrderEmployeeDetailResponse.Product> orderProducts) {
        OrderEmployeeDetailResponse orderEmployeeDetailResponse = new OrderEmployeeDetailResponse();
        orderEmployeeDetailResponse.setProducts(orderProducts);
        orderEmployeeDetailResponse.setDeliveryMode(order.getDeliveryMode());
        orderEmployeeDetailResponse.setOrderCode(order.getCode());
        orderEmployeeDetailResponse.setWaybill(order.getWaybill());
        orderEmployeeDetailResponse.setDeliveryTime(getLocalDateTimeFromTimestamp(order.getPlannedDeliveryDate()));
        orderEmployeeDetailResponse.setOrderStatus(OrderStateInStore.getOrderStatus(order));
        return orderEmployeeDetailResponse;
    }

    public OrderEmployeeDetailResponse.Product mapToGetOrderEmployeeProduct(Product product,
                                                                            SupplyBoxProduct supplyBoxProduct,
                                                                            Optional<StoreCellProduct> storeCellProductOptional) {
        var packageProcessOptional = orderPackageProcessRepository.findBySupplyBoxProductId(supplyBoxProduct.getId());


        OrderEmployeeDetailResponse.Product orderProduct = new OrderEmployeeDetailResponse.Product();
        orderProduct.setProductName(product.getName());
        orderProduct.setProductArticle(supplyBoxProduct.getArticle());
        orderProduct.setProductCell(storeCellProductOptional.isPresent() ? storeCellProductOptional.get().getStoreCell().getCode() : "N/A");
        orderProduct.setProductVendorCode(product.getVendorCode());
        orderProduct.setPathToProductBarcode(barcodeMapper.getPathToProductBarcode(supplyBoxProduct));
        orderProduct.setPathToBoxBarcode(barcodeMapper.getPathToBoxBarcode(supplyBoxProduct.getSupplyBox()));
        orderProduct.setProductStateInStore(supplyBoxProduct.getState());

        if (packageProcessOptional.isPresent()) {
            var packageInfo = new OrderEmployeeDetailResponse.PackageInfo();

            packageInfo.setStartedAt(packageProcessOptional.get().getStartedAt());
            packageInfo.setFinishedAt(packageProcessOptional.get().getFinishedAt());

            orderProduct.setPackageInfo(packageInfo);
        }

        return orderProduct;
    }

    public String extractVendorCode(OrderEntry orderEntry) {
        var vendorCode = orderEntry.getAttributes().getOffer().getCode();
        if (vendorCode != null && !vendorCode.isBlank()) {
            return vendorCode.split("_")[0];
        }
        return null;
    }

}
