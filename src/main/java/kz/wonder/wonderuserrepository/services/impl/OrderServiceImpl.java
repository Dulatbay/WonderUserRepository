package kz.wonder.wonderuserrepository.services.impl;

import kz.wonder.kaspi.client.api.KaspiApi;
import kz.wonder.kaspi.client.model.OrderState;
import kz.wonder.wonderuserrepository.constants.Utils;
import kz.wonder.wonderuserrepository.dto.params.OrderSearchParams;
import kz.wonder.wonderuserrepository.dto.response.EmployeeOrderResponse;
import kz.wonder.wonderuserrepository.dto.response.OrderDetailResponse;
import kz.wonder.wonderuserrepository.dto.response.OrderEmployeeDetailResponse;
import kz.wonder.wonderuserrepository.dto.response.OrderResponse;
import kz.wonder.wonderuserrepository.entities.*;
import kz.wonder.wonderuserrepository.exceptions.DbObjectNotFoundException;
import kz.wonder.wonderuserrepository.mappers.KaspiOrderMapper;
import kz.wonder.wonderuserrepository.repositories.KaspiOrderRepository;
import kz.wonder.wonderuserrepository.repositories.KaspiTokenRepository;
import kz.wonder.wonderuserrepository.repositories.StoreCellProductRepository;
import kz.wonder.wonderuserrepository.repositories.StoreEmployeeRepository;
import kz.wonder.wonderuserrepository.services.OrderParseService;
import kz.wonder.wonderuserrepository.services.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;


@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {
    private final KaspiOrderRepository kaspiOrderRepository;
    private final KaspiApi kaspiApi;
    private final StoreEmployeeRepository storeEmployeeRepository;
    private final StoreCellProductRepository storeCellProductRepository;
    // service works in one layer
    private final OrderParseService orderParseService;
    private final KaspiTokenRepository kaspiTokenRepository;
    private final KaspiOrderMapper kaspiOrderMapper;


    @Override
    public Page<OrderResponse> getSellerOrdersByKeycloakId(String keycloakId, LocalDate startDate, LocalDate endDate, OrderSearchParams orderSearchParams, PageRequest pageRequest) {
        log.info("Retrieving seller orders by keycloak id: {}", keycloakId);
        startDate = startDate.minusDays(1);
        var kaspiOrderInDb = kaspiOrderRepository.findAllSellerOrders(
                keycloakId,
                Utils.getTimeStampFromLocalDateTime(startDate.atStartOfDay()),
                Utils.getTimeStampFromLocalDateTime(endDate.atStartOfDay()),
                orderSearchParams.getDeliveryMode(),
                orderSearchParams.getOrderBaseStatus() != null ? orderSearchParams.getOrderBaseStatus().name() : null,
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
                orderSearchParams.getOrderBaseStatus() != null ? orderSearchParams.getOrderBaseStatus().name() : null,
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
                    orderParseService.processKaspiOrder(token, order, products);
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
                orderSearchParams.getOrderBaseStatus() != null ? orderSearchParams.getOrderBaseStatus().name() : null,
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
                .map(kaspiOrderProduct -> kaspiOrderMapper.toOrderDetailResponse(kaspiOrderProduct))
                .toList();
    }

    @Override
    public List<OrderDetailResponse> getSellerOrderDetails(String keycloakId, String orderCode) {
        var order = kaspiOrderRepository.findByCode(orderCode)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase(), "Заказ не найден"));

        // todo: сделать проверку на то, что этот keycloak user имеет доступ к этому ордеру

        var kaspiOrderProducts = order.getProducts();

        return kaspiOrderProducts.stream()
                .map(kaspiOrderProduct -> kaspiOrderMapper.toOrderDetailResponse(kaspiOrderProduct))
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


}
