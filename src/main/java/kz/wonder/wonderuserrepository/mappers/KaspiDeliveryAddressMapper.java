package kz.wonder.wonderuserrepository.mappers;

import kz.wonder.kaspi.client.model.OrdersDataResponse;
import kz.wonder.wonderuserrepository.entities.KaspiDeliveryAddress;
import org.springframework.stereotype.Component;

@Component
public class KaspiDeliveryAddressMapper {
    public KaspiDeliveryAddress getKaspiDeliveryAddress(OrdersDataResponse.OrderAttributes orderAttributes) {
        KaspiDeliveryAddress kaspiDeliveryAddress = new KaspiDeliveryAddress();
        kaspiDeliveryAddress.setStreetName(orderAttributes.getDeliveryAddress().getStreetName());
        kaspiDeliveryAddress.setStreetNumber(orderAttributes.getDeliveryAddress().getStreetNumber());
        kaspiDeliveryAddress.setTown(orderAttributes.getDeliveryAddress().getTown());
        kaspiDeliveryAddress.setDistrict(orderAttributes.getDeliveryAddress().getDistrict());
        kaspiDeliveryAddress.setBuilding(orderAttributes.getDeliveryAddress().getBuilding());
        kaspiDeliveryAddress.setApartment(orderAttributes.getDeliveryAddress().getApartment());
        kaspiDeliveryAddress.setFormattedAddress(orderAttributes.getDeliveryAddress().getFormattedAddress());
        kaspiDeliveryAddress.setLatitude(orderAttributes.getDeliveryAddress().getLatitude());
        kaspiDeliveryAddress.setLongitude(orderAttributes.getDeliveryAddress().getLongitude());
        return kaspiDeliveryAddress;
    }
}
