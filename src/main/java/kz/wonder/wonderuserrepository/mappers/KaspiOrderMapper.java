package kz.wonder.wonderuserrepository.mappers;

import kz.wonder.kaspi.client.model.OrdersDataResponse;
import kz.wonder.wonderuserrepository.entities.DeliveryMode;
import kz.wonder.wonderuserrepository.entities.KaspiOrder;
import kz.wonder.wonderuserrepository.entities.KaspiToken;
import kz.wonder.wonderuserrepository.exceptions.DbObjectNotFoundException;
import kz.wonder.wonderuserrepository.repositories.KaspiCityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KaspiOrderMapper {
    private final KaspiDeliveryAddressMapper deliveryAddressMapper;
    private final KaspiStoreMapper kaspiStoreMapper;
    private final KaspiCityRepository kaspiCityRepository;


    public KaspiOrder toKaspiOrder(KaspiToken token, OrdersDataResponse.OrdersDataItem order, OrdersDataResponse.OrderAttributes orderAttributes) {
        KaspiOrder kaspiOrder = new KaspiOrder();
        kaspiOrder.setKaspiId(order.getOrderId());
        kaspiOrder.setCode(orderAttributes.getCode());
        kaspiOrder.setTotalPrice(orderAttributes.getTotalPrice());
        kaspiOrder.setPaymentMode(orderAttributes.getPaymentMode());

        if (orderAttributes.getDeliveryAddress() != null) {
            kaspiOrder.setDeliveryAddress(deliveryAddressMapper.getKaspiDeliveryAddress(orderAttributes));
        }
        var kaspiCity = kaspiCityRepository.findByCode(orderAttributes.getOriginAddress().getCity().getCode())
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND, "Kaspi city not found", ""));
        ;

        var kaspiStore = kaspiStoreMapper.getKaspiStore(orderAttributes.getOriginAddress(), kaspiCity);

        kaspiOrder.setKaspiStore(kaspiStore);
        kaspiOrder.setCreditTerm(orderAttributes.getCreditTerm());
        kaspiOrder.setKaspiCity(kaspiCity);
        kaspiOrder.setPlannedDeliveryDate(orderAttributes.getPlannedDeliveryDate());
        kaspiOrder.setCreationDate(orderAttributes.getCreationDate());
        kaspiOrder.setDeliveryCostForSeller(orderAttributes.getDeliveryCostForSeller());
        kaspiOrder.setIsKaspiDelivery(orderAttributes.getIsKaspiDelivery());
        kaspiOrder.setDeliveryMode(DeliveryMode.buildDeliveryMode(orderAttributes.getDeliveryMode(), orderAttributes.getIsKaspiDelivery()));
        kaspiOrder.setSignatureRequired(orderAttributes.getSignatureRequired());
        kaspiOrder.setWaybill(orderAttributes.getKaspiDelivery().getWaybill());
        kaspiOrder.setCourierTransmissionDate(orderAttributes.getKaspiDelivery().getCourierTransmissionDate());
        kaspiOrder.setCourierTransmissionPlanningDate(orderAttributes.getKaspiDelivery().getCourierTransmissionPlanningDate());
        kaspiOrder.setWaybillNumber(orderAttributes.getKaspiDelivery().getWaybillNumber());
        kaspiOrder.setExpress(orderAttributes.getKaspiDelivery().getExpress());
        kaspiOrder.setReturnedToWarehouse(orderAttributes.getKaspiDelivery().getReturnedToWarehouse());
        kaspiOrder.setFirstMileCourier(orderAttributes.getKaspiDelivery().getFirstMileCourier());
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
}
