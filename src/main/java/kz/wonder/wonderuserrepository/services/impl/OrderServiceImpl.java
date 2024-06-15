package kz.wonder.wonderuserrepository.services.impl;

import kz.wonder.kaspi.client.api.KaspiApi;
import kz.wonder.kaspi.client.model.OrderState;
import kz.wonder.kaspi.client.model.OrdersDataResponse;
import kz.wonder.kaspi.client.model.response.Order.OrderEntry;
import kz.wonder.wonderuserrepository.constants.Utils;
import kz.wonder.wonderuserrepository.dto.params.OrderSearchParams;
import kz.wonder.wonderuserrepository.dto.response.EmployeeOrderResponse;
import kz.wonder.wonderuserrepository.dto.response.OrderDetailResponse;
import kz.wonder.wonderuserrepository.dto.response.OrderEmployeeDetailResponse;
import kz.wonder.wonderuserrepository.dto.response.OrderResponse;
import kz.wonder.wonderuserrepository.entities.*;
import kz.wonder.wonderuserrepository.entities.enums.KaspiProductUnitType;
import kz.wonder.wonderuserrepository.entities.enums.ProductStateInStore;
import kz.wonder.wonderuserrepository.exceptions.DbObjectNotFoundException;
import kz.wonder.wonderuserrepository.mappers.KaspiDeliveryAddressMapper;
import kz.wonder.wonderuserrepository.mappers.KaspiOrderMapper;
import kz.wonder.wonderuserrepository.mappers.KaspiStoreMapper;
import kz.wonder.wonderuserrepository.repositories.*;
import kz.wonder.wonderuserrepository.services.ApplicationPropertyService;
import kz.wonder.wonderuserrepository.services.KaspiProductCategoryService;
import kz.wonder.wonderuserrepository.services.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static kz.wonder.wonderuserrepository.constants.ValueConstants.UPDATE_ORDERS_IGNORE_TIME_PROPERTY_NAME;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {
    private final KaspiOrderRepository kaspiOrderRepository;
    private final KaspiApi kaspiApi;
    private final KaspiOrderProductRepository kaspiOrderProductRepository;
    private final ProductRepository productRepository;
    private final SupplyBoxProductsRepository supplyBoxProductsRepository;
    private final StoreEmployeeRepository storeEmployeeRepository;
    private final StoreCellProductRepository storeCellProductRepository;
    private final KaspiTokenRepository kaspiTokenRepository;
    private final KaspiOrderMapper kaspiOrderMapper;
    private final KaspiDeliveryAddressMapper kaspiDeliveryAddressMapper;
    private final KaspiCityRepository kaspiCityRepository;
    private final KaspiStoreMapper kaspiStoreMapper;
    private final KaspiStoreRepository kaspiStoreRepository;
    private final ApplicationPropertyService applicationPropertyService;
    private final KaspiProductCategoryService kaspiProductCategoryService;


    @Override
    public Page<OrderResponse> getSellerOrdersByKeycloakId(String keycloakId, LocalDate startDate, LocalDate endDate, OrderSearchParams orderSearchParams, PageRequest pageRequest) {
        log.info("Retrieving seller orders by keycloak id: {}", keycloakId);
        startDate = startDate.minusDays(1);
        var kaspiOrderInDb = kaspiOrderRepository.findAllSellerOrders(
                keycloakId,
                Utils.getTimeStampFromLocalDateTime(startDate.atStartOfDay()),
                Utils.getTimeStampFromLocalDateTime(endDate.atStartOfDay()),
                orderSearchParams.getDeliveryMode(),
                orderSearchParams.getSearchValue().toLowerCase(),
                orderSearchParams.isByOrderCode(),
                orderSearchParams.isByShopName(),
                orderSearchParams.isByStoreAddress(),
                orderSearchParams.isByProductName(),
                orderSearchParams.isByProductArticle(),
                orderSearchParams.isByProductVendorCode(),
                pageRequest);
        log.info("Seller orders successfully retrieved. keycloakID: {}", keycloakId);
        // todo: переделать оптовую цену
        return kaspiOrderInDb
                .map(kaspiOrder -> getOrderResponse(kaspiOrder, 0.0));
    }

    @Override
    public Page<OrderResponse> getAdminOrdersByKeycloakId(String keycloakId, LocalDate startDate, LocalDate endDate, OrderSearchParams orderSearchParams, PageRequest pageRequest) {
        log.info("Retrieving admin orders by keycloak id: {}", keycloakId);


        var orders = kaspiOrderRepository.findAllAdminOrders(
                keycloakId,
                Utils.getTimeStampFromLocalDateTime(startDate.atStartOfDay()),
                Utils.getTimeStampFromLocalDateTime(endDate.atStartOfDay()),
                orderSearchParams.getDeliveryMode(),
                orderSearchParams.getSearchValue().toLowerCase(),
                orderSearchParams.isByOrderCode(),
                orderSearchParams.isByShopName(),
                orderSearchParams.isByStoreAddress(),
                orderSearchParams.isByProductName(),
                orderSearchParams.isByProductArticle(),
                orderSearchParams.isByProductVendorCode(),
                pageRequest);

        log.info("Admin orders successfully retrieved. keycloakID: {}", keycloakId);

        return orders.map(order -> getOrderResponse(order, 0.0));
    }


    private void processTokenOrders(KaspiToken token, long startDate, long currentTime, int pageNumber, OrderState state) {
        try {
            var ordersDataResponse = kaspiApi.getOrders(token.getToken(), startDate, currentTime, state, pageNumber, 100).block();
            log.info("Found orders data, sellerName: {}, startDate: {}, endDate: {}, orderState: {}, pageNumber: {}, ordersDataResponse.data size: {}",
                    token.getSellerName(),
                    startDate,
                    currentTime,
                    state,
                    pageNumber,
                    ordersDataResponse.getData().size());
            var orders = ordersDataResponse.getData();
            var products = ordersDataResponse.getIncluded();


            log.info("orders count: {}, products count: {}", orders.size(), products.size());

            for (var order : orders) {
                try {
                    var kaspiOrder = saveKaspiOrder(order, token);
                    boolean storeNotFound = (kaspiOrder.getKaspiCity() == null || kaspiOrder.getKaspiStore() == null);

                    var orderEntries = products.stream().filter(p -> p.getId().startsWith(order.getOrderId()) && !kaspiOrderProductRepository.existsByKaspiId(p.getId())).toList();

                    for (var orderEntry : orderEntries) {
                        processOrderProduct(token, kaspiOrder, orderEntry);

                        if (storeNotFound) {
                            var pointOfServiceResponse = kaspiApi.getStoreById(orderEntry.getId(), token.getToken()).block();

                            assert pointOfServiceResponse != null;
                            var kaspiCity = kaspiCityRepository.findByKaspiId(pointOfServiceResponse.getCityRelationship().getData().getId())
                                    .orElseThrow(() -> new RuntimeException("Kaspi Store not found"));

                            var kaspiStore = kaspiStoreMapper.findByAddress(pointOfServiceResponse.getAddress(), kaspiCity);

                            if (kaspiStore.isEmpty())
                                kaspiStore = kaspiStoreRepository.findByOriginAddressId(pointOfServiceResponse.getId());

                            if (kaspiStore.isEmpty()) {
                                log.info("Create store with kaspiEndPoint: {}", kaspiOrder.getCode());

                                var createdKaspiStore = kaspiStoreMapper.createStoreByParamsOfOrder(pointOfServiceResponse.getId(),
                                        pointOfServiceResponse.getDisplayName(),
                                        pointOfServiceResponse.getAddress(),
                                        kaspiCity,
                                        null
                                );

                                kaspiOrder.setKaspiStore(createdKaspiStore);
                                kaspiOrderRepository.save(kaspiOrder);
                            } else {
                                kaspiOrder.setKaspiStore(kaspiStore.get());
                                kaspiOrderRepository.save(kaspiOrder);
                            }

                            storeNotFound = false;
                        }

                    }
                } catch (Exception e) {
                    log.error("Initializing error, sellerName: {}, startDate: {}, endDate: {}, orderState: {}, pageNumber: {}, order: {}",
                            token.getSellerName(),
                            startDate,
                            currentTime,
                            state,
                            pageNumber,
                            order.getAttributes().getCode(),
                            e);
                }
            }

            if (ordersDataResponse.getMeta().getPageCount() > pageNumber + 1) {
                processTokenOrders(token, startDate, currentTime, pageNumber + 1, state);
            }

            log.info("Initializing finished, sellerName: {}, startDate: {}, endDate: {}, orderState: {}, pageNumber: {}, ordersDataResponse.data size: {}",
                    token.getSellerName(),
                    startDate,
                    currentTime,
                    state,
                    pageNumber,
                    orders.size());

        } catch (Exception e) {
            log.error("Initializing error, sellerName: {}, startDate: {}, endDate: {}, orderState: {}, pageNumber: {}",
                    token.getSellerName(),
                    startDate,
                    currentTime,
                    state,
                    pageNumber, e);
        }
    }

    @Override
    public Page<EmployeeOrderResponse> getEmployeeOrders(String keycloakId, LocalDate startDate, LocalDate endDate, OrderSearchParams orderSearchParams, PageRequest pageRequest) {
        var orders = kaspiOrderRepository.findAllEmployeeOrders(
                keycloakId,
                Utils.getTimeStampFromLocalDateTime(startDate.atStartOfDay()),
                Utils.getTimeStampFromLocalDateTime(endDate.atStartOfDay()),
                orderSearchParams.getDeliveryMode(),
                orderSearchParams.getSearchValue() != null ? orderSearchParams.getSearchValue().toLowerCase().trim() : null,
                orderSearchParams.isByOrderCode(),
                orderSearchParams.isByShopName(),
                orderSearchParams.isByStoreAddress(),
                orderSearchParams.isByProductName(),
                orderSearchParams.isByProductArticle(),
                orderSearchParams.isByProductVendorCode(),
                pageRequest);

        return orders.map(this::getEmployeeOrderResponse);
    }

    @Override
    public List<OrderDetailResponse> getAdminOrderDetails(String keycloakId, String orderCode) {
        var order = kaspiOrderRepository.findByCode(orderCode)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase(), "Заказ не найден"));

        // todo: сделать проверку на то, что этот keycloak user имеет доступ к этому ордеру

        var kaspiOrderProducts = order.getProducts();

        return kaspiOrderProducts.stream()
                .map(kaspiOrderProduct -> kaspiOrderMapper.toOrderDetailResponse(kaspiOrderProduct, order))
                .toList();
    }

    @Override
    public List<OrderDetailResponse> getSellerOrderDetails(String keycloakId, String orderCode) {
        var order = kaspiOrderRepository.findByCode(orderCode)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase(), "Заказ не найден"));

        // todo: сделать проверку на то, что этот keycloak user имеет доступ к этому ордеру

        var kaspiOrderProducts = order.getProducts();

        return kaspiOrderProducts.stream()
                .map(kaspiOrderProduct -> kaspiOrderMapper.toOrderDetailResponse(kaspiOrderProduct, order))
                .toList();
    }

    @Override
    public OrderEmployeeDetailResponse getEmployeeOrderDetails(String keycloakId, String orderCode) {
        var employee = storeEmployeeRepository.findByWonderUserKeycloakId(keycloakId)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.FORBIDDEN, HttpStatus.FORBIDDEN.getReasonPhrase(), "Вы не являетесь сотрудником пользователя"));

        var order = kaspiOrderRepository.findByCode(orderCode)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase(), "Заказ не найден"));

        var isEmployeeWorkInThisStore = order.getKaspiStore().getId().equals(employee.getKaspiStore().getId());

        if (!isEmployeeWorkInThisStore) {
            throw new IllegalArgumentException("Заказ не найден");
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

        return kaspiOrderMapper.toOrderEmployeeDetailResponse(order, orderProducts);
    }

    private OrderEmployeeDetailResponse.Product getOrderEmployeeProduct(Optional<Product> product, Optional<SupplyBoxProduct> supplyBoxProductOptional, Optional<StoreCellProduct> storeCellProductOptional) {
        return kaspiOrderMapper.mapToGetOrderEmployeeProduct(product, supplyBoxProductOptional, storeCellProductOptional);
    }

    @Override
    public void updateOrders() {
        try {
            log.info("Updating orders started");
            long currentTime = System.currentTimeMillis();
            long durationOf14Days = Duration.ofDays(14).toMillis();
            long durationOf5Days = Duration.ofDays(5).toMillis();

            var tokens = kaspiTokenRepository.findAllWithFetching();

            log.info("Found {} tokens", tokens.size());

            tokens.forEach(token -> {
                if (token.getToken().equals("tester")) return;
                try {
                    CompletableFuture<Void> kaspiDeliveryFuture = CompletableFuture.runAsync(() ->
                            this.processTokenOrders(token, currentTime - durationOf14Days, currentTime, 0, OrderState.KASPI_DELIVERY)
                    );
                    CompletableFuture<Void> pickupFuture = CompletableFuture.runAsync(() ->
                            this.processTokenOrders(token, currentTime - durationOf14Days, currentTime, 0, OrderState.PICKUP)
                    );
                    CompletableFuture<Void> archiveFuture = CompletableFuture.runAsync(() ->
                            this.processTokenOrders(token, currentTime - durationOf5Days, currentTime, 0, OrderState.ARCHIVE)
                    );

                    CompletableFuture.allOf(kaspiDeliveryFuture, pickupFuture, archiveFuture).join();
                } catch (Exception ex) {
                    log.error("Error processing orders for token: {}", token, ex);
                }
            });
        } catch (Exception ex) {
            log.error("Error updating orders", ex);
        }
    }

    private OrderResponse getOrderResponse(KaspiOrder kaspiOrder, Double tradePrice) {
        return kaspiOrderMapper.mapToOrderResponse(kaspiOrder, tradePrice);
    }

    private EmployeeOrderResponse getEmployeeOrderResponse(KaspiOrder kaspiOrder) {
        return kaspiOrderMapper.mapToEmployeeOrderResponse(kaspiOrder);
    }


    private KaspiOrder saveKaspiOrder(OrdersDataResponse.OrdersDataItem order, KaspiToken token) {
        var orderAttributes = order.getAttributes();
        var optionalKaspiOrder = kaspiOrderRepository.findByCode(orderAttributes.getCode());

        if (optionalKaspiOrder.isPresent()) {
            return kaspiOrderMapper.updateKaspiOrder(optionalKaspiOrder.get(), token, order, orderAttributes);
        } else {
            var kaspiOrder = kaspiOrderMapper.saveKaspiOrder(token, order, orderAttributes);

            detectStoreAndCity(orderAttributes, kaspiOrder);

            return kaspiOrderRepository.save(kaspiOrder);
        }
    }

    private void detectStoreAndCity(OrdersDataResponse.OrderAttributes orderAttributes, KaspiOrder kaspiOrder) {
        if (orderAttributes.getDeliveryAddress() != null) {
            kaspiOrder.setDeliveryAddress(kaspiDeliveryAddressMapper.getKaspiDeliveryAddress(orderAttributes));
        }

        // if the originAddress is null, then an order delivery type is pickup
        if (orderAttributes.getOriginAddress() != null) {

            var kaspiCity = kaspiCityRepository.findByCode(orderAttributes.getOriginAddress().getCity().getCode())
                    .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND, "Kaspi city not found", ""));

            var kaspiStore = kaspiStoreMapper.getKaspiStore(orderAttributes, orderAttributes.getOriginAddress(), kaspiCity);

            kaspiOrder.setKaspiStore(kaspiStore);
            kaspiOrder.setKaspiCity(kaspiCity);
        } else {
            var pickupPointId = orderAttributes.getPickupPointId();

            var kaspiStoreOptional = kaspiStoreRepository.findByPickupPointId(pickupPointId);

            if (kaspiStoreOptional.isPresent()) {
                kaspiOrder.setKaspiStore(kaspiStoreOptional.get());
                kaspiOrder.setKaspiCity(kaspiStoreOptional.get().getKaspiCity());
            }
        }
    }


    @Transactional
    public void processOrderProduct(KaspiToken token, KaspiOrder kaspiOrder, OrderEntry orderEntry) {
        var vendorCode = kaspiOrderMapper.extractVendorCode(orderEntry);

        var product = productRepository
                .findByOriginalVendorCodeAndKeycloakIdAndDeletedIsFalse(vendorCode,
                        token.getWonderUser().getKeycloakId())
                .orElse(null);


        if (product != null) {

            var supplyBoxProductOptional = supplyBoxProductsRepository.findFirstByStoreIdAndProductIdAndState(kaspiOrder.getKaspiStore().getId(), product.getId(), ProductStateInStore.ACCEPTED);


            if (supplyBoxProductOptional.isPresent()) {
                var supplyBoxProduct = supplyBoxProductOptional.get();
                var sellAt = Utils.getLocalDateTimeFromTimestamp(kaspiOrder.getCreationDate());


                log.info("accepted time: {}, now: {}", supplyBoxProduct.getAcceptedTime(), sellAt);

                if (applicationPropertyService.getApplicationPropertyByName(UPDATE_ORDERS_IGNORE_TIME_PROPERTY_NAME).getValue().equals("true")
                        || (supplyBoxProduct.getAcceptedTime() != null && supplyBoxProduct.getAcceptedTime().isBefore(sellAt))) {
                    log.info("supplyBoxProduct to save: {}", supplyBoxProduct.getId());
                }

                supplyBoxProduct.setState(ProductStateInStore.WAITING_FOR_ASSEMBLY);
                supplyBoxProduct.setKaspiOrder(kaspiOrder);
                supplyBoxProductsRepository.save(supplyBoxProduct);
                log.info("SOLD MENTIONED, product id: {}, order code: {}", product.getId(), kaspiOrder.getCode());

                KaspiOrderProduct kaspiOrderProduct = new KaspiOrderProduct();
                kaspiOrderProduct.setOrder(kaspiOrder);
                kaspiOrderProduct.setProduct(product);
                kaspiOrderProduct.setKaspiId(orderEntry.getId());
                kaspiOrderProduct.setQuantity(orderEntry.getAttributes().getQuantity());
                kaspiOrderProduct.setCategory(kaspiProductCategoryService.findOrCreate(orderEntry.getAttributes().getCategory().getCode(), orderEntry.getAttributes().getCategory().getTitle()));
                kaspiOrderProduct.setBasePrice(orderEntry.getAttributes().getBasePrice());
                kaspiOrderProduct.setDeliveryCost(orderEntry.getAttributes().getDeliveryCost());
                kaspiOrderProduct.setEntryNumber(orderEntry.getAttributes().getEntryNumber());
                kaspiOrderProduct.setUnitType(KaspiProductUnitType.getByDescription(orderEntry.getAttributes().getUnitType()));
                kaspiOrderProduct.setWeight(orderEntry.getAttributes().getWeight());
                kaspiOrderProduct.setSupplyBoxProduct(supplyBoxProduct);

                kaspiOrderProductRepository.save(kaspiOrderProduct);
            }


        }

        kaspiOrderRepository.save(kaspiOrder);
    }


}
