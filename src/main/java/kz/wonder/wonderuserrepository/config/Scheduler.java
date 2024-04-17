package kz.wonder.wonderuserrepository.config;

import kz.wonder.kaspi.client.api.KaspiApi;
import kz.wonder.kaspi.client.model.OrderState;
import kz.wonder.kaspi.client.model.OrdersDataResponse;
import kz.wonder.wonderuserrepository.entities.*;
import kz.wonder.wonderuserrepository.exceptions.DbObjectNotFoundException;
import kz.wonder.wonderuserrepository.repositories.KaspiCityRepository;
import kz.wonder.wonderuserrepository.repositories.KaspiOrderRepository;
import kz.wonder.wonderuserrepository.repositories.KaspiTokenRepository;
import kz.wonder.wonderuserrepository.services.CityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Component
@Slf4j
public class Scheduler {

    private final CityService cityService;
    private final KaspiApi kaspiApi;
    private final KaspiOrderRepository kaspiOrderRepository;
    private final KaspiCityRepository kaspiCityRepository;
    private final KaspiTokenRepository kaspiTokenRepository;

//    @Scheduled(fixedRate = 3600000 * 24) // 24 hours
//    public void updateCitiesFromKaspiApi() {
//        cityService.syncWithKaspi();
//    }

    @Scheduled(fixedRate = 36000 * 10)
    public void updateOrders() {
        log.info("Updating orders started");
        long currentTime = System.currentTimeMillis();
        long twoWeeksTime = ((3600 * 60) * 24) * 14; // 2 weeks
        long startDate = currentTime - twoWeeksTime;

        var tokens = kaspiTokenRepository.findAll();
        tokens.forEach(token -> processTokenOrders(token, startDate, currentTime));
    }

    int createdCount = 0, updatedCount = 0;

    private void processTokenOrders(KaspiToken token, long startDate, long currentTime) {
        kaspiApi.getOrders(token.getToken(), startDate, currentTime, OrderState.KASPI_DELIVERY, 0, 100)
                .subscribe(
                        ordersDataResponse -> {
                            log.info("Found orders data, startDate: {}, endDate: {}, ordersDataResponse.data size: {}",
                                    startDate,
                                    currentTime,
                                    ordersDataResponse.getData().size());
                            ordersDataResponse.getData().forEach(order -> processOrder(order, token));
                            log.info("Initializing orders finished, created count: {}, updated count: {}", createdCount, updatedCount);
                            createdCount = 0;
                            updatedCount = 0;
                        },
                        error -> log.error("Error updating orders: {}", error.getMessage(), error)
                );
    }

    private void processOrder(OrdersDataResponse.OrdersDataItem order, KaspiToken token) {
        var orderAttributes = order.getAttributes();
        var optionalKaspiOrder = kaspiOrderRepository.findByCode(orderAttributes.getCode());
        if (optionalKaspiOrder.isPresent()) {
            var kaspiOrder = optionalKaspiOrder.get();
            if (kaspiOrder.getUpdatedAt().isAfter(LocalDateTime.now().minusMinutes(15))) {
                updatedCount++;
                getKaspiOrderByParams(order, orderAttributes, kaspiOrder);
            }
        } else {
            createdCount++;
            KaspiOrder kaspiOrder = new KaspiOrder();
            getKaspiOrderByParams(order, orderAttributes, kaspiOrder);
        }
    }

    private void getKaspiOrderByParams(OrdersDataResponse.OrdersDataItem order, OrdersDataResponse.OrderAttributes orderAttributes, KaspiOrder kaspiOrder) {
        kaspiOrder.setKaspiId(order.getOrderId());
        kaspiOrder.setCode(orderAttributes.getCode());
        kaspiOrder.setTotalPrice(orderAttributes.getTotalPrice());
        kaspiOrder.setPaymentMode(orderAttributes.getPaymentMode());

        if (orderAttributes.getDeliveryAddress() != null) {
            kaspiOrder.setAddressStreetName(orderAttributes.getDeliveryAddress().getStreetName());
            kaspiOrder.setAddressStreetNumber(orderAttributes.getDeliveryAddress().getStreetNumber());
            kaspiOrder.setAddressTown(orderAttributes.getDeliveryAddress().getTown());
            kaspiOrder.setAddressDistrict(orderAttributes.getDeliveryAddress().getDistrict());
            kaspiOrder.setAddressBuilding(orderAttributes.getDeliveryAddress().getBuilding());
            kaspiOrder.setAddressApartment(orderAttributes.getDeliveryAddress().getApartment());
            kaspiOrder.setAddressFormattedAddress(orderAttributes.getDeliveryAddress().getFormattedAddress());
            kaspiOrder.setAddressLatitude(orderAttributes.getDeliveryAddress().getLatitude());
            kaspiOrder.setAddressLongitude(orderAttributes.getDeliveryAddress().getLongitude());
            kaspiOrder.setDeliveryAddress(getKaspiDeliveryAddress(orderAttributes));
        }

        kaspiOrder.setCreditTerm(orderAttributes.getCreditTerm());
        kaspiOrder.setKaspiCity(getKaspiCity(orderAttributes));
        kaspiOrder.setPlannedDeliveryDate(orderAttributes.getPlannedDeliveryDate());
        kaspiOrder.setCreationDate(orderAttributes.getCreationDate());
        kaspiOrder.setDeliveryCostForSeller(orderAttributes.getDeliveryCostForSeller());
        kaspiOrder.setIsKaspiDelivery(orderAttributes.getIsKaspiDelivery());
        kaspiOrder.setDeliveryMode(orderAttributes.getDeliveryMode());
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
        kaspiOrderRepository.save(kaspiOrder);
    }


    private KaspiCity getKaspiCity(OrdersDataResponse.OrderAttributes orderAttributes) {
        return kaspiCityRepository.findByCode(orderAttributes.getOriginAddress().getCity().getCode())
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND, "Kaspi city not found", ""));
    }

    private static @NotNull KaspiDeliveryAddress getKaspiDeliveryAddress(OrdersDataResponse.OrderAttributes orderAttributes) {
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