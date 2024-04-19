package kz.wonder.wonderuserrepository.services.impl;

import kz.wonder.kaspi.client.api.KaspiApi;
import kz.wonder.kaspi.client.model.Order.OrderEntry;
import kz.wonder.kaspi.client.model.OrderState;
import kz.wonder.kaspi.client.model.OrdersDataResponse;
import kz.wonder.wonderuserrepository.dto.response.OrderResponse;
import kz.wonder.wonderuserrepository.entities.*;
import kz.wonder.wonderuserrepository.exceptions.DbObjectNotFoundException;
import kz.wonder.wonderuserrepository.repositories.*;
import kz.wonder.wonderuserrepository.services.OrderService;
import kz.wonder.wonderuserrepository.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static kz.wonder.wonderuserrepository.constants.ValueConstants.ZONE_ID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {
    private final KaspiOrderRepository kaspiOrderRepository;
    private final UserService userService;
    private final KaspiApi kaspiApi;
    private final KaspiCityRepository kaspiCityRepository;
    private final KaspiStoreRepository kaspiStoreRepository;
    private final UserRepository userRepository;
    private final KaspiOrderProductRepository kaspiOrderProductRepository;
    private final ProductRepository productRepository;
    private final SupplyBoxProductsRepository supplyBoxProductsRepository;


    // todo: переделать этот говно код
    @Value("${application.admin-keycloak-id}")
    private String adminKeycloakId;

    private WonderUser admin;

    int createdCount = 0, updatedCount = 0;

    @Override
    public List<OrderResponse> getSellerOrdersByKeycloakId(String keycloakId, LocalDate startDate, LocalDate endDate) {
        log.info("Retrieving seller orders by keycloak id: {}", keycloakId);
        startDate = startDate.minusDays(1);
        var kaspiOrderInDb = kaspiOrderRepository.findAllByWonderUserKeycloakIdAndCreationDateBetween(keycloakId, Timestamp.valueOf(startDate.atStartOfDay()).getTime(), Timestamp.valueOf(endDate.atStartOfDay()).getTime());
        log.info("Seller orders successfully retrieved. keycloakID: {}", keycloakId);
        return kaspiOrderInDb
                .stream()
                .map(kaspiOrder -> getOrderResponse(kaspiOrder, 0.0)) // todo: переделать оптовую цену
                .toList();
    }

    private static OrderResponse getOrderResponse(KaspiOrder kaspiOrder, Double tradePrice) {

        return OrderResponse.builder()
                .id(kaspiOrder.getId())
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

    @Override
    public List<OrderResponse> getAdminOrdersByKeycloakId(String keycloakId, LocalDate startDate, LocalDate endDate) {
        log.info("Retrieving admin orders by keycloak id: {}", keycloakId);
        var wonderUser = userService.getUserByKeycloakId(keycloakId);
        var stores = wonderUser.getStores();

        final LocalDate finalStartDate = startDate.minusDays(1);

        var result = stores.stream()
                .flatMap(store -> store.getOrders().stream()
                        .filter(kaspiOrder -> {
                            LocalDate kaspiOrderDate = Instant.ofEpochMilli(kaspiOrder.getCreationDate()).atZone(ZONE_ID).toLocalDate();
                            return (kaspiOrderDate.isAfter(finalStartDate) && kaspiOrderDate.isBefore(endDate));
                        })
                        .map(order -> getOrderResponse(order, 0.0))
                )
                .collect(Collectors.toList());


        log.info("Admin orders successfully retrieved. keycloakID: {}", keycloakId);
        return result;
    }


    @Override
    public void processTokenOrders(KaspiToken token, long startDate, long currentTime) {
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
            if (kaspiOrder.getUpdatedAt().isBefore(LocalDateTime.now().minusMinutes(15))) {
                try {
                    getKaspiOrderByParams(token, order, orderAttributes, kaspiOrder, orderEntry);

                    updatedCount++;
                } catch (Exception e) {
                    log.error("Error processing order: {}", e.getMessage(), e);
                }
            }
        } else {
            try {
                KaspiOrder kaspiOrder = new KaspiOrder();
                getKaspiOrderByParams(token, order, orderAttributes, kaspiOrder, orderEntry);
                createdCount++;
            } catch (Exception e) {
                log.error("Error processing order: {}", e.getMessage(), e);
            }
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

        var kaspiStore = getKaspiStore(orderAttributes.getOriginAddress(), getKaspiCity(orderAttributes));

        kaspiOrder.setKaspiStore(kaspiStore);
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
        // todo: внедрить мапперы в проект

//        var product = productRepository.findByVendorCodeAndKeycloakId(orderEntry.getAttributes().getOffer().getCode(), kaspiOrder.getKaspiId())
//                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND, "Product not found", ""));


        // Null because this product doesn't exist in our db
        var product = productRepository.findByVendorCodeAndKeycloakId(orderEntry.getAttributes().getOffer().getCode(), kaspiOrder.getKaspiId())
                .orElse(null);

        if (product != null) {
            var optionalSupplyBoxProduct = supplyBoxProductsRepository.findByParams(product.getVendorCode(), token.getWonderUser().getKeycloakId(), kaspiStore.getId(), ProductStateInStore.ACCEPTED);

            // если этого баркода у нас нет в складе, то останавливаемся
            if (optionalSupplyBoxProduct.isEmpty()) {
                throw new DbObjectNotFoundException(HttpStatus.NOT_FOUND, HttpStatus.NOT_FOUND.getReasonPhrase(), "Product with code " + product.getVendorCode() + " not found in kaspi store(maybe user didn't create supply with product)");
            }

            var supplyBoxProduct = optionalSupplyBoxProduct.get();
            supplyBoxProduct.setState(ProductStateInStore.SOLD); // продукт продан у нас
            supplyBoxProductsRepository.save(supplyBoxProduct);
        }

        KaspiOrderProduct kaspiOrderProduct = kaspiOrderProductRepository.findByProductIdAndOrderId(product == null ? null : product.getId(), kaspiOrder.getId())
                .orElse(new KaspiOrderProduct());
        kaspiOrderProduct.setOrder(kaspiOrder);
        kaspiOrderProduct.setProduct(product);
        kaspiOrderProduct.setQuantity(orderEntry.getAttributes().getQuantity());

        kaspiOrderRepository.save(kaspiOrder);
        kaspiOrderProductRepository.save(kaspiOrderProduct);
    }

    private @NotNull KaspiCity getKaspiCity(OrdersDataResponse.OrderAttributes orderAttributes) {
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

    public @NotNull KaspiStore getKaspiStore(OrdersDataResponse.Address address, KaspiCity kaspiCity) {
        String apartment = address.getAddress().getApartment();
        String streetName = address.getAddress().getStreetName();
        String streetNumber = address.getAddress().getStreetNumber();
        String town = address.getAddress().getTown();
        String building = address.getAddress().getBuilding();
        String district = address.getAddress().getDistrict();

        var optionalKaspiStore = kaspiStoreRepository.findByStoreAddress(apartment, streetName, streetNumber, town, building, district);

        if (optionalKaspiStore.isPresent()) {
            return optionalKaspiStore.get();
        } else {
            KaspiStore kaspiStore = getStore(address, kaspiCity);
            return kaspiStoreRepository.save(kaspiStore);
        }
    }

    private @NotNull KaspiStore getStore(OrdersDataResponse.Address address, KaspiCity kaspiCity) {
        // todo: этот store создается для какого юзера(сделаю пока для main админа)
        KaspiStore kaspiStore = new KaspiStore();


        kaspiStore.setKaspiId(address.getDisplayName());
        kaspiStore.setStreetName(address.getAddress().getStreetName());
        kaspiStore.setStreetNumber(address.getAddress().getStreetNumber());
        kaspiStore.setTown(address.getAddress().getTown());
        kaspiStore.setDistrict(address.getAddress().getDistrict());
        kaspiStore.setBuilding(address.getAddress().getBuilding());
        kaspiStore.setApartment(address.getAddress().getApartment());
        kaspiStore.setFormattedAddress(address.getAddress().getFormattedAddress());
        kaspiStore.setLatitude(address.getAddress().getLatitude());
        kaspiStore.setLongitude(address.getAddress().getLongitude());
        kaspiStore.setKaspiCity(kaspiCity);


        if (admin == null)
            admin = userService.getUserByKeycloakId(adminKeycloakId);

        kaspiStore.setWonderUser(admin);
        return kaspiStore;
    }
}
