package kz.wonder.wonderuserrepository.config;

import kz.wonder.kaspi.client.api.KaspiApi;
import kz.wonder.kaspi.client.model.Order.OrderEntry;
import kz.wonder.kaspi.client.model.OrderState;
import kz.wonder.kaspi.client.model.OrdersDataResponse;
import kz.wonder.wonderuserrepository.entities.*;
import kz.wonder.wonderuserrepository.exceptions.DbObjectNotFoundException;
import kz.wonder.wonderuserrepository.repositories.*;
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
    private final KaspiStoreRepository kaspiStoreRepository;
    private final UserRepository userRepository;
    private final KaspiOrderProductRepository kaspiOrderProductRepository;
    private final ProductRepository productRepository;
    private final SupplyBoxProductsRepository supplyBoxProductsRepository;

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

        log.info("Found {} tokens", tokens.size());

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
                            var orders = ordersDataResponse.getData();
                            var products = ordersDataResponse.getIncluded();

                            int i = 0;
                            for (var order : orders) {
                                processOrder(order, token, products.get(i));
                                i++;
                            }

                            log.info("Initializing orders finished, created count: {}, updated count: {}", createdCount, updatedCount);
                            createdCount = 0;
                            updatedCount = 0;
                        },
                        error -> log.error("Error updating orders: {}", error.getMessage(), error)
                );
    }

    private void processOrder(OrdersDataResponse.OrdersDataItem order, KaspiToken token, OrderEntry orderEntry) {
        var orderAttributes = order.getAttributes();
        var optionalKaspiOrder = kaspiOrderRepository.findByCode(orderAttributes.getCode());
        if (optionalKaspiOrder.isPresent()) {
            var kaspiOrder = optionalKaspiOrder.get();
            if (kaspiOrder.getUpdatedAt().isAfter(LocalDateTime.now().minusMinutes(15))) {
                updatedCount++;
                getKaspiOrderByParams(token, order, orderAttributes, kaspiOrder, orderEntry);
            }
        } else {
            createdCount++;
            KaspiOrder kaspiOrder = new KaspiOrder();
            getKaspiOrderByParams(token, order, orderAttributes, kaspiOrder, orderEntry);
        }
    }

    private void getKaspiOrderByParams(KaspiToken token, OrdersDataResponse.OrdersDataItem order, OrdersDataResponse.OrderAttributes orderAttributes, KaspiOrder kaspiOrder, OrderEntry orderEntry) {
        kaspiOrder.setKaspiId(order.getOrderId());
        kaspiOrder.setCode(orderAttributes.getCode());
        kaspiOrder.setTotalPrice(orderAttributes.getTotalPrice());
        kaspiOrder.setPaymentMode(orderAttributes.getPaymentMode());

        if (orderAttributes.getDeliveryAddress() != null) {
            kaspiOrder.setDeliveryAddress(getKaspiDeliveryAddress(orderAttributes));
        }
        var kaspiCity = getKaspiCity(orderAttributes);

        var wonderUser = token.getWonderUser();

        kaspiOrder.setKaspiStore(getKaspiStore(orderAttributes.getOriginAddress(), getKaspiCity(orderAttributes), wonderUser));
        kaspiOrder.setCreditTerm(orderAttributes.getCreditTerm());
        kaspiOrder.setKaspiCity(kaspiCity);
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
        kaspiOrder.setWonderUser(token.getWonderUser());

        // todo: может ли один и тот же артикул товара(каспи) быть у двух разных продавцов


//        var product = productRepository.findByVendorCodeAndKeycloakId(orderEntry.getAttributes().getOffer().getCode(), kaspiOrder.getKaspiId())
//                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND, "Product not found", ""));


        // Null because this product doesn't exist in our db
        var product = productRepository.findByVendorCodeAndKeycloakId(orderEntry.getAttributes().getOffer().getCode(), kaspiOrder.getKaspiId())
                .orElse(null);

        if (product != null) {
            var optionalSupplyBoxProduct = supplyBoxProductsRepository.findByProductVendorCodeAndProductKeycloakId(product.getVendorCode(), token.getWonderUser().getKeycloakId());

            // если этого баркода у нас нет в складе, то останавливаемся
            if (optionalSupplyBoxProduct.isEmpty()) {
                return;
            }

            var supplyBoxProduct = optionalSupplyBoxProduct.get();
            supplyBoxProduct.setState(ProductStateInStore.SOLD); // продукт продан у нас
            supplyBoxProductsRepository.save(supplyBoxProduct);
        }

        KaspiOrderProduct kaspiOrderProduct = new KaspiOrderProduct();
        kaspiOrderProduct.setOrder(kaspiOrder);
        kaspiOrderProduct.setProduct(product);
        kaspiOrderProduct.setQuantity(kaspiOrderProduct.getQuantity());

        kaspiOrderRepository.save(kaspiOrder);
        kaspiOrderProductRepository.save(kaspiOrderProduct);
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


    public @NotNull KaspiStore getKaspiStore(OrdersDataResponse.Address address, KaspiCity kaspiCity, WonderUser wonderUser) {
        var optionalKaspiStore = kaspiStoreRepository.findByStoreAddress(address.getAddress().getApartment(),
                address.getAddress().getStreetName(),
                address.getAddress().getStreetNumber(),
                address.getAddress().getTown(),
                address.getAddress().getDistrict(),
                address.getAddress().getBuilding());

        if (optionalKaspiStore.isPresent()) {
            return optionalKaspiStore.get();
        } else {
            WonderUser persistedWonderUser = userRepository.findById(wonderUser.getId())
                    .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND, "WonderUser not found", ""));
            KaspiStore kaspiStore = getStore(address, kaspiCity, persistedWonderUser);
            return kaspiStoreRepository.save(kaspiStore);
        }
    }

    private static @NotNull KaspiStore getStore(OrdersDataResponse.Address address, KaspiCity kaspiCity, WonderUser wonderUser) {
        KaspiStore kaspiStore = new KaspiStore();
        kaspiStore.setKaspiId(address.getDisplayName());
        kaspiStore.setKaspiCity(kaspiCity);
        kaspiStore.setTown(address.getAddress().getTown());
        kaspiStore.setDistrict(address.getAddress().getDistrict());
        kaspiStore.setBuilding(address.getAddress().getBuilding());
        kaspiStore.setApartment(address.getAddress().getApartment());
        kaspiStore.setFormattedAddress(address.getAddress().getFormattedAddress());
        kaspiStore.setWonderUser(wonderUser);
        return kaspiStore;
    }
}