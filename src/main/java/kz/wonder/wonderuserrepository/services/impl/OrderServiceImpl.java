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
import kz.wonder.wonderuserrepository.mappers.KaspiOrderMapper;
import kz.wonder.wonderuserrepository.repositories.*;
import kz.wonder.wonderuserrepository.services.OrderService;
import kz.wonder.wonderuserrepository.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
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
    private final KaspiOrderProductRepository kaspiOrderProductRepository;
    private final ProductRepository productRepository;
    private final SupplyBoxProductsRepository supplyBoxProductsRepository;
    private final StoreEmployeeRepository storeEmployeeRepository;
    private final StoreCellProductRepository storeCellProductRepository;
    private final KaspiTokenRepository kaspiTokenRepository;
    private final KaspiOrderMapper kaspiOrderMapper;


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
                                var kaspiOrder = getKaspiOrder(order, token);
                                orderEntries.forEach(orderEntry -> {
                                    processOrderProduct(token, kaspiOrder, orderEntry);
                                });
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

    private KaspiOrder getKaspiOrder(OrdersDataResponse.OrdersDataItem order, KaspiToken token) {
        var orderAttributes = order.getAttributes();
        var optionalKaspiOrder = kaspiOrderRepository.findByCode(orderAttributes.getCode());

        if (optionalKaspiOrder.isPresent()) {
            var updatedKaspiOrder = kaspiOrderMapper.toKaspiOrder(token, order, orderAttributes);
            updatedKaspiOrder.setId(optionalKaspiOrder.get().getId());
            updatedKaspiOrder.setCreatedAt(optionalKaspiOrder.get().getCreatedAt());
            updatedKaspiOrder = kaspiOrderRepository.save(updatedKaspiOrder);
            updatedCount++;
            return updatedKaspiOrder;
        } else {
            var toCreateKaspiOrder = kaspiOrderMapper.toKaspiOrder(token, order, orderAttributes);
            toCreateKaspiOrder = kaspiOrderRepository.save(toCreateKaspiOrder);
            createdCount++;
            return toCreateKaspiOrder;
        }
    }


    private void processOrderProduct(KaspiToken token, KaspiOrder kaspiOrder, OrderEntry orderEntry) {
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
            var supplyBoxProductList = supplyBoxProductsRepository.findAllByStoreIdAndProductId(kaspiOrder.getKaspiStore().getId(), product.getId());


            var sellAt = Instant.ofEpochMilli(kaspiOrder.getCreationDate()).atZone(ZONE_ID).toLocalDateTime();
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
                log.info("SOLD MENTIONED, product id: {}, order code: {}", product.getId(), kaspiOrder.getCode());
            }
        }

        kaspiOrderRepository.save(kaspiOrder);


        if (product != null && supplyBoxProductToSave != null) {
            KaspiOrderProduct kaspiOrderProduct = kaspiOrderProductRepository.findByProductIdAndOrderIdAndSupplyBoxProductId(product.getId(), kaspiOrder.getId(), supplyBoxProductToSave.getId())
                    .orElse(new KaspiOrderProduct());
            kaspiOrderProduct.setOrder(kaspiOrder);
            kaspiOrderProduct.setProduct(product);
            kaspiOrderProduct.setQuantity(orderEntry.getAttributes().getQuantity());
            kaspiOrderProduct.setSupplyBoxProduct(supplyBoxProductToSave);
            kaspiOrderProductRepository.save(kaspiOrderProduct);
        }

    }


}
