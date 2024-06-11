package kz.wonder.wonderuserrepository.services.impl;

import kz.wonder.kaspi.client.api.KaspiApi;
import kz.wonder.kaspi.client.model.Order.OrderEntry;
import kz.wonder.kaspi.client.model.OrderState;
import kz.wonder.kaspi.client.model.OrdersDataResponse;
import kz.wonder.wonderuserrepository.constants.Utils;
import kz.wonder.wonderuserrepository.dto.response.EmployeeOrderResponse;
import kz.wonder.wonderuserrepository.dto.response.OrderDetailResponse;
import kz.wonder.wonderuserrepository.dto.response.OrderEmployeeDetailResponse;
import kz.wonder.wonderuserrepository.dto.response.OrderResponse;
import kz.wonder.wonderuserrepository.entities.*;
import kz.wonder.wonderuserrepository.exceptions.DbObjectNotFoundException;
import kz.wonder.wonderuserrepository.mappers.KaspiOrderMapper;
import kz.wonder.wonderuserrepository.mappers.OrderMapper;
import kz.wonder.wonderuserrepository.repositories.*;
import kz.wonder.wonderuserrepository.services.OrderService;
import kz.wonder.wonderuserrepository.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static kz.wonder.wonderuserrepository.constants.ValueConstants.ZONE_ID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {
    private final KaspiOrderRepository kaspiOrderRepository;
    private final UserService userService;
    private final KaspiApi kaspiApi;
    private final KaspiOrderProductRepository kaspiOrderProductRepository;
    private final ProductRepository productRepository;
    private final SupplyBoxProductsRepository supplyBoxProductsRepository;
    private final StoreEmployeeRepository storeEmployeeRepository;
    private final StoreCellProductRepository storeCellProductRepository;
    private final KaspiTokenRepository kaspiTokenRepository;
    private final KaspiOrderMapper kaspiOrderMapper;


    @Override
    public Page<OrderResponse> getSellerOrdersByKeycloakId(String keycloakId, LocalDate startDate, LocalDate endDate, DeliveryMode deliveryMode, PageRequest pageRequest) {
        log.info("Retrieving seller orders by keycloak id: {}", keycloakId);
        startDate = startDate.minusDays(1);
        var kaspiOrderInDb = kaspiOrderRepository.findAllSellerOrders(keycloakId, Timestamp.valueOf(startDate.atStartOfDay()).getTime(), Timestamp.valueOf(endDate.atStartOfDay()).getTime(), deliveryMode, pageRequest);
        log.info("Seller orders successfully retrieved. keycloakID: {}", keycloakId);
        // todo: переделать оптовую цену
        return kaspiOrderInDb
                .map(kaspiOrder -> getOrderResponse(kaspiOrder, 0.0));
    }

    @Override
    public Page<OrderResponse> getAdminOrdersByKeycloakId(String keycloakId, LocalDate startDate, LocalDate endDate, DeliveryMode deliveryMode, PageRequest pageRequest) {
        log.info("Retrieving admin orders by keycloak id: {}", keycloakId);


        var orders = kaspiOrderRepository.findAllAdminOrders(keycloakId,
                Utils.getTimeStampFromLocalDateTime(startDate.atStartOfDay()),
                Utils.getTimeStampFromLocalDateTime(endDate.atStartOfDay()),
                deliveryMode,
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
                var pairResult = saveKaspiOrder(order, token);
                if (pairResult.getValue()) {
                    var orderEntries = products.stream().filter(p -> p.getId().startsWith(order.getOrderId())).toList();
                    orderEntries.stream()
                            .filter(orderEntry -> !kaspiOrderProductRepository.existsByKaspiId(orderEntry.getId()))
                            .forEach(orderEntry -> {
                                processOrderProduct(token, pairResult.getKey(), orderEntry);
                            });
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
            log.info("Initializing error, sellerName: {}, startDate: {}, endDate: {}, orderState: {}, pageNumber: {}",
                    token.getSellerName(),
                    startDate,
                    currentTime,
                    state,
                    pageNumber);

            log.error("Error processing token orders", e);
        }


    }

    @Override
    public List<EmployeeOrderResponse> getEmployeeOrders(String keycloakId, LocalDate startDate, LocalDate endDate, DeliveryMode deliveryMode) {
        var employee = storeEmployeeRepository.findByWonderUserKeycloakId(keycloakId)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase(), "Пользователь-сотрудник магазина не найден"));


        var orders = kaspiOrderRepository.findAllEmployeeOrders(employee.getWonderUser().getKeycloakId(),
                Utils.getTimeStampFromLocalDateTime(startDate.atStartOfDay()),
                Utils.getTimeStampFromLocalDateTime(endDate.atStartOfDay()),
                deliveryMode);

        return orders.stream().map(OrderServiceImpl::getEmployeeOrderResponse).toList();
    }

    @Override
    public List<OrderDetailResponse> getAdminOrderDetails(String keycloakId, String orderCode) {
        var order = kaspiOrderRepository.findByCode(orderCode)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase(), "Заказ не найден"));

        // todo: сделать проверку на то, что этот keycloak user имеет доступ к этому ордеру

        var kaspiOrderProducts = order.getProducts();

        return kaspiOrderProducts.stream()
                .map(kaspiOrderProduct -> OrderMapper.toOrderDetailResponse(kaspiOrderProduct, order))
                .toList();
    }

    @Override
    public List<OrderDetailResponse> getSellerOrderDetails(String keycloakId, String orderCode) {
        var order = kaspiOrderRepository.findByCode(orderCode)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase(), "Заказ не найден"));

        // todo: сделать проверку на то, что этот keycloak user имеет доступ к этому ордеру

        var kaspiOrderProducts = order.getProducts();

        return kaspiOrderProducts.stream()
                .map(kaspiOrderProduct -> OrderMapper.toOrderDetailResponse(kaspiOrderProduct, order))
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

        return OrderMapper.toOrderEmployeeDetailResponse(order, orderProducts);
    }

    private static @NotNull OrderEmployeeDetailResponse.Product getOrderEmployeeProduct(Optional<Product> product, Optional<SupplyBoxProduct> supplyBoxProductOptional, Optional<StoreCellProduct> storeCellProductOptional) {
        return OrderMapper.mapToGetOrderEmployeeProduct(product, supplyBoxProductOptional, storeCellProductOptional);
    }

    @Override
    public void updateOrders() {
        try {
            log.info("Updating orders started");
            long currentTime = System.currentTimeMillis();
            long durationOf14Days = Duration.ofDays(14).toMillis();
            long durationOf5Days = Duration.ofDays(5).toMillis();

            var tokens = kaspiTokenRepository.findAll();

            log.info("Found {} tokens", tokens.size());

            tokens.parallelStream().forEach(token -> {
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

    private static OrderResponse getOrderResponse(KaspiOrder kaspiOrder, Double tradePrice) {
        return OrderMapper.mapToOrderResponse(kaspiOrder, tradePrice);
    }

    private static EmployeeOrderResponse getEmployeeOrderResponse(KaspiOrder kaspiOrder) {
        return OrderMapper.mapToEmployeeOrderResponse(kaspiOrder);
    }


    private Pair<KaspiOrder, Boolean> saveKaspiOrder(OrdersDataResponse.OrdersDataItem order, KaspiToken token) {
        var orderAttributes = order.getAttributes();
        var optionalKaspiOrder = kaspiOrderRepository.findByCode(orderAttributes.getCode());

        if (optionalKaspiOrder.isPresent()) {
            var updatedKaspiOrder = kaspiOrderMapper.toKaspiOrder(token, order, orderAttributes);
            updatedKaspiOrder.setId(optionalKaspiOrder.get().getId());
            updatedKaspiOrder.setCreatedAt(optionalKaspiOrder.get().getCreatedAt());
            updatedKaspiOrder.setOrderAssemble(optionalKaspiOrder.get().getOrderAssemble());

            return Pair.of(kaspiOrderRepository.save(updatedKaspiOrder), Objects.equals(updatedKaspiOrder.getStatus(), "ACCEPTED_BY_MERCHANT"));
        } else {
            var toCreateKaspiOrder = kaspiOrderMapper.toKaspiOrder(token, order, orderAttributes);
            kaspiOrderRepository.save(toCreateKaspiOrder);
            return Pair.of(kaspiOrderRepository.save(toCreateKaspiOrder), Objects.equals(toCreateKaspiOrder.getStatus(), "ACCEPTED_BY_MERCHANT"));
        }
    }


    private void processOrderProduct(KaspiToken token, KaspiOrder kaspiOrder, OrderEntry orderEntry) {
        // todo: замечать отмену заказов в шелудер

        var vendorCode = OrderMapper.extractVendorCode(orderEntry);

        var product = productRepository
                .findByOriginalVendorCodeAndKeycloakId(vendorCode,
                        token.getWonderUser().getKeycloakId())
                .orElse(null);

        if (product != null)
            log.info("product id: {}, keycloak id: {}, store id: {}", product.getId(), token.getWonderUser().getKeycloakId(), kaspiOrder.getKaspiStore().getId());

        SupplyBoxProduct supplyBoxProductToSave = null;
        if (product != null) {

            var supplyBoxProductList = supplyBoxProductsRepository.findAllByStoreIdAndProductIdAndState(kaspiOrder.getKaspiStore().getId(), product.getId(), ProductStateInStore.ACCEPTED);


            var sellAt = Instant.ofEpochMilli(kaspiOrder.getCreationDate()).atZone(ZONE_ID).toLocalDateTime();
            // todo: просто получить сразу один продукт поставки сразу, не перебирая весь цикл

            for (var supplyBoxProduct : supplyBoxProductList) {
                if (ProductStateInStore.ACCEPTED == supplyBoxProduct.getState()) {
                    log.info("accepted time: {}, now: {}", supplyBoxProduct.getAcceptedTime(), sellAt);
//                    if (supplyBoxProduct.getAcceptedTime() != null && supplyBoxProduct.getAcceptedTime().isBefore(sellAt)) {
                    supplyBoxProductToSave = supplyBoxProduct;
                    log.info("supplyBoxProductToSave: {}", supplyBoxProductToSave.getId());
                    break;
//                    }
                }
            }


            if (supplyBoxProductToSave != null) {
                supplyBoxProductToSave.setState(ProductStateInStore.WAITING_FOR_ASSEMBLY);
                supplyBoxProductToSave.setKaspiOrder(kaspiOrder);
                supplyBoxProductsRepository.save(supplyBoxProductToSave);
                log.info("SOLD MENTIONED, product id: {}, order code: {}", product.getId(), kaspiOrder.getCode());
            }
        }

        kaspiOrderRepository.save(kaspiOrder);


        if (product != null && supplyBoxProductToSave != null) {
            KaspiOrderProduct kaspiOrderProduct = new KaspiOrderProduct();
            kaspiOrderProduct.setOrder(kaspiOrder);
            kaspiOrderProduct.setProduct(product);
            kaspiOrderProduct.setKaspiId(orderEntry.getId());
            kaspiOrderProduct.setQuantity(orderEntry.getAttributes().getQuantity());
            kaspiOrderProduct.setSupplyBoxProduct(supplyBoxProductToSave);
            kaspiOrderProductRepository.save(kaspiOrderProduct);
        }

    }


}
