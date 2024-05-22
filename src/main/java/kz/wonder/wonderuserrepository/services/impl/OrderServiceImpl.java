package kz.wonder.wonderuserrepository.services.impl;

import kz.wonder.kaspi.client.api.KaspiApi;
import kz.wonder.kaspi.client.model.Order.OrderEntry;
import kz.wonder.kaspi.client.model.OrderState;
import kz.wonder.kaspi.client.model.OrdersDataResponse;
import kz.wonder.wonderuserrepository.dto.response.EmployeeOrderResponse;
import kz.wonder.wonderuserrepository.dto.response.OrderDetailResponse;
import kz.wonder.wonderuserrepository.dto.response.OrderEmployeeDetailResponse;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static kz.wonder.wonderuserrepository.constants.Utils.getLocalDateTimeFromTimestamp;
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
    private final KaspiOrderProductRepository kaspiOrderProductRepository;
    private final ProductRepository productRepository;
    private final SupplyBoxProductsRepository supplyBoxProductsRepository;
    private final StoreEmployeeRepository storeEmployeeRepository;
    private final StoreCellProductRepository storeCellProductRepository;
    private final KaspiTokenRepository kaspiTokenRepository;
    // todo: переделать этот говно код
    @Value("${application.admin-keycloak-id}")
    private String adminKeycloakId;
    private WonderUser admin;


    @Override
    public Page<OrderResponse> getSellerOrdersByKeycloakId(String keycloakId, LocalDate startDate, LocalDate endDate, PageRequest pageRequest) {
        log.info("Retrieving seller orders by keycloak id: {}", keycloakId);
        startDate = startDate.minusDays(1);
        var kaspiOrderInDb = kaspiOrderRepository.findAllByWonderUserKeycloakIdAndCreationDateBetween(keycloakId, Timestamp.valueOf(startDate.atStartOfDay()).getTime(), Timestamp.valueOf(endDate.atStartOfDay()).getTime(), pageRequest);
        log.info("Seller orders successfully retrieved. keycloakID: {}", keycloakId);
        // todo: переделать оптовую цену
        return kaspiOrderInDb
                .map(kaspiOrder -> getOrderResponse(kaspiOrder, 0.0));
    }


    private static OrderResponse getOrderResponse(KaspiOrder kaspiOrder, Double tradePrice) {

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

    private static EmployeeOrderResponse getEmployeeOrderResponse(KaspiOrder kaspiOrder) {
        EmployeeOrderResponse orderResponse = new EmployeeOrderResponse();
        orderResponse.setOrderCode(kaspiOrder.getCode());
        orderResponse.setOrderCreatedAt(Instant.ofEpochMilli(kaspiOrder.getCreationDate()).atZone(ZONE_ID).toLocalDateTime());
        orderResponse.setOrderStatus(kaspiOrder.getStatus());
        orderResponse.setOrderToSendTime(getLocalDateTimeFromTimestamp(kaspiOrder.getCourierTransmissionPlanningDate()));
        orderResponse.setDeliveryType(kaspiOrder.getDeliveryMode());

        return orderResponse;
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

    @Override
    public Page<OrderResponse> getAdminOrdersByKeycloakId(String keycloakId, LocalDate startDate, LocalDate endDate, PageRequest pageRequest) {
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

        return new PageImpl<>(result, pageRequest, result.size());
    }

    int createdCount = 0, updatedCount = 0;

    private void processTokenOrders(KaspiToken token, long startDate, long currentTime, int pageNumber) {
        kaspiApi.getOrders(token.getToken(), startDate, currentTime, OrderState.KASPI_DELIVERY, pageNumber, 100)
                .subscribe(
                        ordersDataResponse -> {
                            log.info("Found orders data, startDate: {}, endDate: {}, ordersDataResponse.data size: {}",
                                    startDate,
                                    currentTime,
                                    ordersDataResponse.getData().size());
                            var orders = ordersDataResponse.getData();
                            var products = ordersDataResponse.getIncluded();

                            log.info("orders count: {}, products count: {}", orders.size(), products.size());

                            for (var order : orders) {
                                var orderEntries = products.stream().filter(p -> p.getId().startsWith(order.getOrderId())).toList();
                                orderEntries.forEach(orderEntry -> processOrder(order, token, orderEntry));
                            }

                            log.info("Initializing orders finished, created count: {}, updated count: {}", createdCount, updatedCount);
                            createdCount = 0;
                            updatedCount = 0;

                            if (ordersDataResponse.getMeta().getPageCount() > pageNumber + 1) {
                                processTokenOrders(token, startDate, currentTime, pageNumber + 1);
                            }
                        },
                        error -> log.error("Error updating orders: {}", error.getMessage(), error)
                );
    }

    @Override
    public List<EmployeeOrderResponse> getEmployeeOrders(String keycloakId, LocalDate startDate, LocalDate endDate) {
        var employee = storeEmployeeRepository.findByWonderUserKeycloakId(keycloakId)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase(), "Store employee user not found"));

        var store = employee.getKaspiStore();

        final LocalDate finalStartDate = startDate.minusDays(1);

        // todo: переделать запрос на уровень репозитория

        return store.getOrders().stream()
                .filter(kaspiOrder -> {
                    LocalDate kaspiOrderDate = Instant.ofEpochMilli(kaspiOrder.getCreationDate()).atZone(ZONE_ID).toLocalDate();
                    var products = kaspiOrder.getProducts();
                    return (kaspiOrderDate.isAfter(finalStartDate) && kaspiOrderDate.isBefore(endDate) && !products.isEmpty() && products.stream().noneMatch(kp -> kp.getProduct() == null || kp.getSupplyBoxProduct() == null));
                })
                .map(OrderServiceImpl::getEmployeeOrderResponse)
                .toList();
    }

    @Override
    public List<OrderDetailResponse> getAdminOrderDetails(String keycloakId, String orderCode) {
        var order = kaspiOrderRepository.findByCode(orderCode)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase(), "Order not found"));

        // todo: сделать проверку на то, что этот keycloak user имеет доступ к этому ордеру

        var kaspiOrderProducts = order.getProducts();

        return kaspiOrderProducts.stream().map(kaspiOrderProduct -> {
            var product = kaspiOrderProduct.getProduct();
            var supplyBoxProduct = kaspiOrderProduct.getSupplyBoxProduct();
            if (supplyBoxProduct == null)
                supplyBoxProduct = new SupplyBoxProduct();

            var storeCellProductOptional = storeCellProductRepository.findBySupplyBoxProductId(supplyBoxProduct.getId());


            OrderDetailResponse orderDetailResponse = new OrderDetailResponse();
            orderDetailResponse.setProductName(product == null ? "N\\A" : product.getName());
            orderDetailResponse.setProductArticle(supplyBoxProduct.getArticle() == null ? "N\\A" : supplyBoxProduct.getArticle());
            orderDetailResponse.setCellCode(storeCellProductOptional.isPresent() ? storeCellProductOptional.get().getStoreCell().getCode() : "N\\A");
            orderDetailResponse.setProductVendorCode(product == null ? "N\\A" : product.getVendorCode());
            orderDetailResponse.setProductTradePrice(product == null ? 0 : product.getTradePrice());
            orderDetailResponse.setProductSellPrice(order.getTotalPrice()); // todo: тут прибыль от заказа, как достать прибыль именно от одного продукта?(посмотреть потом в апи)
            orderDetailResponse.setIncome(orderDetailResponse.getProductSellPrice() - orderDetailResponse.getProductTradePrice());
            return orderDetailResponse;
        }).toList();
    }

    @Override
    public List<OrderDetailResponse> getSellerOrderDetails(String keycloakId, String orderCode) {
        var order = kaspiOrderRepository.findByCode(orderCode)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase(), "Order not found"));

        // todo: сделать проверку на то, что этот keycloak user имеет доступ к этому ордеру

        var kaspiOrderProducts = order.getProducts();

        return kaspiOrderProducts.stream().map(kaspiOrderProduct -> {
            var product = kaspiOrderProduct.getProduct();
            var supplyBoxProduct = kaspiOrderProduct.getSupplyBoxProduct();
            var storeCellProductOptional = storeCellProductRepository.findBySupplyBoxProductId(supplyBoxProduct.getId());

            OrderDetailResponse orderDetailResponse = new OrderDetailResponse();
            orderDetailResponse.setProductName(product.getName());
            orderDetailResponse.setProductArticle(supplyBoxProduct.getArticle());
            orderDetailResponse.setCellCode(storeCellProductOptional.isPresent() ? storeCellProductOptional.get().getStoreCell().getCode() : "N\\A");
            orderDetailResponse.setProductVendorCode(product.getVendorCode());
            orderDetailResponse.setProductTradePrice(product.getTradePrice());
            orderDetailResponse.setProductSellPrice(order.getTotalPrice()); // todo: тут прибыль от заказа, как достать прибыль именно от одного продукта?(посмотреть потом в апи)
            orderDetailResponse.setIncome(orderDetailResponse.getProductSellPrice() - orderDetailResponse.getProductTradePrice());
            return orderDetailResponse;
        }).toList();
    }

    @Override
    public OrderEmployeeDetailResponse getEmployeeOrderDetails(String keycloakId, String orderCode) {
        var employee = storeEmployeeRepository.findByWonderUserKeycloakId(keycloakId)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.FORBIDDEN, HttpStatus.FORBIDDEN.getReasonPhrase(), "You are not employee user"));

        var order = kaspiOrderRepository.findByCode(orderCode)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase(), "Order not found"));

        var isEmployeeWorkInThisStore = order.getKaspiStore().getId().equals(employee.getKaspiStore().getId());

        if (!isEmployeeWorkInThisStore) {
            throw new IllegalArgumentException("Order not found");
        }

        var orderProducts = order.getProducts()
                .stream()
                .map(kaspiOrderProduct -> {
                    var productOptional = Optional.ofNullable(kaspiOrderProduct.getProduct());
                    var supplyBoxProductOptional = Optional.ofNullable(kaspiOrderProduct.getSupplyBoxProduct());
                    var storeCellProductOptional = storeCellProductRepository.findBySupplyBoxProductId(supplyBoxProductOptional.isEmpty() ? -1L : supplyBoxProductOptional.get().getId());

                    return getOrderEmployeeProduct(productOptional, supplyBoxProductOptional, storeCellProductOptional);
                })
                .toList();

        OrderEmployeeDetailResponse orderEmployeeDetailResponse = new OrderEmployeeDetailResponse();
        orderEmployeeDetailResponse.setProducts(orderProducts);
        orderEmployeeDetailResponse.setDeliveryMode(order.getDeliveryMode());
        orderEmployeeDetailResponse.setDeliveryTime(getLocalDateTimeFromTimestamp(order.getPlannedDeliveryDate()));

        return orderEmployeeDetailResponse;
    }

    private static @NotNull OrderEmployeeDetailResponse.Product getOrderEmployeeProduct(Optional<Product> product, Optional<SupplyBoxProduct> supplyBoxProductOptional, Optional<StoreCellProduct> storeCellProductOptional) {
        OrderEmployeeDetailResponse.Product orderProduct = new OrderEmployeeDetailResponse.Product();
        orderProduct.setProductName(product.isEmpty() ? "N\\A" : product.get().getName());
        orderProduct.setProductArticle(supplyBoxProductOptional.isEmpty() ? "N\\A" : supplyBoxProductOptional.get().getArticle());
        orderProduct.setProductCell(storeCellProductOptional.isPresent() ? storeCellProductOptional.get().getStoreCell().getCode() : "N\\A");
        orderProduct.setProductVendorCode(product.isEmpty() ? "N\\A" : product.get().getVendorCode());
        return orderProduct;
    }

    @Override
    public void updateOrders() {
        try {
            log.info("Updating orders started");
            long currentTime = System.currentTimeMillis();
            long duration = Duration.ofDays(14).toMillis();
            long startDate = currentTime - duration;

            var tokens = kaspiTokenRepository.findAll();

            log.info("Found {} tokens", tokens.size());

            tokens.forEach(token -> {
                try {
                    this.processTokenOrders(token, startDate, currentTime, 0);
                } catch (Exception ex) {
                    log.error("Error processing orders for token: {}", token, ex);
                }
            });
        } catch (Exception ex) {
            log.error("Error updating orders", ex);
        }
    }

    private void processOrder(OrdersDataResponse.OrdersDataItem order, KaspiToken token, OrderEntry orderEntry) {
        var orderAttributes = order.getAttributes();
        var optionalKaspiOrder = kaspiOrderRepository.findByCode(orderAttributes.getCode());
        if (optionalKaspiOrder.isPresent()) {
            var kaspiOrder = optionalKaspiOrder.get();
            try {
                getKaspiOrderByParams(token, order, orderAttributes, kaspiOrder, orderEntry);
                updatedCount++;
            } catch (Exception e) {
                log.error("Error processing order: {}", e.getMessage(), e);
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

        // todo: внедрить мапперы в проект
        // todo: замечать отмену заказов в шелудер


        var vendorCode = orderEntry.getAttributes().getOffer().getCode();


        if (vendorCode != null && !vendorCode.isBlank()) {
            vendorCode = vendorCode.split("_")[0];
        }

        var product = productRepository
                .findByOriginalVendorCodeAndKeycloakId(vendorCode,
                        token.getWonderUser().getKeycloakId())
                .orElse(null);


        SupplyBoxProduct supplyBoxProductToSave = null;
        if (product != null) {
            var supplyBoxProductList = supplyBoxProductsRepository.findAllByStoreIdAndProductId(kaspiStore.getId(), product.getId());


            var sellAt = Instant.ofEpochMilli(orderAttributes.getCreationDate()).atZone(ZONE_ID).toLocalDateTime();
            // todo: просто получить сразу один продукт поставки сразу, не перебирая весь цикл

            for (var supplyBoxProduct : supplyBoxProductList) {
                if (ProductStateInStore.ACCEPTED == supplyBoxProduct.getState()) {
                    log.info("accepted time: {}, now: {}", supplyBoxProduct.getAcceptedTime(), sellAt);
                    if (supplyBoxProduct.getAcceptedTime() != null && supplyBoxProduct.getAcceptedTime().isBefore(sellAt)) {
                        supplyBoxProductToSave = supplyBoxProduct;
                        log.info("supplyBoxProductToSave: {}", supplyBoxProductToSave.getId());
                        break;
                    }
                }
            }


            if (supplyBoxProductToSave != null) {
                supplyBoxProductToSave.setState(ProductStateInStore.WAITING_FOR_ASSEMBLY);
                supplyBoxProductsRepository.save(supplyBoxProductToSave);
                log.info("SOLD MENTIONED, product id: {}, order code: {}", product.getId(), order.getOrderId());
            }
        }


        // todo: here is bug with kaspi order products(duplicates in db)
        KaspiOrderProduct kaspiOrderProduct = kaspiOrderProductRepository.findByProductIdAndOrderId(product == null ? null : product.getId(), kaspiOrder.getId())
                .orElse(new KaspiOrderProduct());
        kaspiOrderProduct.setOrder(kaspiOrder);
        kaspiOrderProduct.setProduct(product);
        kaspiOrderProduct.setQuantity(orderEntry.getAttributes().getQuantity());
        kaspiOrderProduct.setSupplyBoxProduct(supplyBoxProductToSave);

        kaspiOrderRepository.save(kaspiOrder);
        kaspiOrderProductRepository.save(kaspiOrderProduct);
    }

    private @NotNull KaspiCity getKaspiCity(OrdersDataResponse.OrderAttributes orderAttributes) {
        return kaspiCityRepository.findByCode(orderAttributes.getOriginAddress().getCity().getCode())
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND, "Kaspi city not found", ""));
    }

    public @NotNull KaspiStore getKaspiStore(OrdersDataResponse.Address address, KaspiCity kaspiCity) {
        var optionalKaspiStore = kaspiStoreRepository.findByOriginAddressId(address.getId());


        if (optionalKaspiStore.isEmpty()) {

            String apartment = address.getAddress().getApartment() == null ? null : address.getAddress().getApartment().trim();
            String streetName = address.getAddress().getStreetName() == null ? null : address.getAddress().getStreetName().trim();
            String streetNumber = address.getAddress().getStreetNumber() == null ? null : address.getAddress().getStreetNumber().trim();
            String town = address.getAddress().getTown() == null ? null : address.getAddress().getTown().trim();
            String building = address.getAddress().getBuilding() == null ? null : address.getAddress().getBuilding().trim();
            String district = address.getAddress().getDistrict() == null ? null : address.getAddress().getDistrict().trim();

            address.getAddress().setApartment(apartment);
            address.getAddress().setStreetName(streetName);
            address.getAddress().setStreetNumber(streetNumber);
            address.getAddress().setTown(town);
            address.getAddress().setBuilding(building);
            address.getAddress().setDistrict(district);

            optionalKaspiStore = kaspiStoreRepository.findByStoreAddress(apartment, streetName, streetNumber, town, building, district);
        }

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
        kaspiStore.setOriginAddressId(address.getId());

        if (admin == null)
            admin = userService.getUserByKeycloakId(adminKeycloakId);

        kaspiStore.setWonderUser(admin);
        return kaspiStore;
    }
}
