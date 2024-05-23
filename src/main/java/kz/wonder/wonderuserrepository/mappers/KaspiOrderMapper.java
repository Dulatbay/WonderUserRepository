package kz.wonder.wonderuserrepository.mappers;

import kz.wonder.kaspi.client.model.OrdersDataResponse;
import kz.wonder.wonderuserrepository.entities.DeliveryMode;
import kz.wonder.wonderuserrepository.entities.KaspiOrder;
import kz.wonder.wonderuserrepository.entities.KaspiToken;
import kz.wonder.wonderuserrepository.exceptions.DbObjectNotFoundException;
import kz.wonder.wonderuserrepository.repositories.KaspiCityRepository;
import kz.wonder.wonderuserrepository.repositories.KaspiStoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class KaspiOrderMapper {
    private final KaspiDeliveryAddressMapper deliveryAddressMapper;
    private final KaspiStoreMapper kaspiStoreMapper;
    private final KaspiCityRepository kaspiCityRepository;
    private final KaspiStoreRepository kaspiStoreRepository;


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

            var kaspiStore = kaspiStoreMapper.getKaspiStore(orderAttributes.getOriginAddress(), kaspiCity);

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
            var divided = pickupPointId.split("_");
            if (divided.length == 2) {
                var kaspiId = divided[1];

                var kaspiStoreOptional = kaspiStoreRepository.findByKaspiIdAndWonderUserKeycloakId(kaspiId, token.getWonderUser().getKeycloakId());

                if (kaspiStoreOptional.isPresent()) {
                    kaspiOrder.setKaspiStore(kaspiStoreOptional.get());
                    kaspiOrder.setKaspiCity(kaspiStoreOptional.get().getKaspiCity());
                }
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
}
