package kz.wonder.wonderuserrepository.services.impl;

import kz.wonder.wonderuserrepository.constants.Utils;
import kz.wonder.wonderuserrepository.dto.params.DurationParams;
import kz.wonder.wonderuserrepository.dto.response.*;
import kz.wonder.wonderuserrepository.entities.KaspiOrder;
import kz.wonder.wonderuserrepository.entities.ProductPrice;
import kz.wonder.wonderuserrepository.entities.Supply;
import kz.wonder.wonderuserrepository.entities.SupplyBoxProduct;
import kz.wonder.wonderuserrepository.repositories.*;
import kz.wonder.wonderuserrepository.services.StatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static kz.wonder.wonderuserrepository.constants.Utils.getTimeStampFromLocalDateTime;
import static kz.wonder.wonderuserrepository.constants.ValueConstants.DECIMAL_FORMAT;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {
    private final SupplyBoxProductsRepository supplyBoxProductsRepository;
    private final SupplyRepository supplyRepository;
    private final KaspiOrderRepository kaspiOrderRepository;
    private final ProductRepository productRepository;
    private final KaspiOrderProductRepository kaspiOrderProductRepository;
    private final ProductPriceRepository productPriceRepository;

    @Override
    public AdminSalesInformation getAdminSalesInformation(String keycloakId, DurationParams durationParams) {
        var duration = durationParams.getDuration();

        // filter work
        // startPast -------> startCurrent --------> end
        var end = LocalDate.now().plusDays(1).atStartOfDay();
        var startCurrent = end.minus(duration);
        var startPast = startCurrent.minus(duration);


        var listOfProducts = supplyBoxProductsRepository.findAllAdminSells(keycloakId, startPast, end);
        var supplies = supplyRepository.findAllByCreatedAtBetweenAndKaspiStore_WonderUserKeycloakId(startPast, end, keycloakId);

        log.info("startPast: {}, startCurrent: {}, end: {}, listOfProduct size: {}, supplies size: {}",
                startPast, startCurrent, end, listOfProducts.size(), supplies.size());


        AdminSalesInformation adminSalesInformation = new AdminSalesInformation();

        adminSalesInformation.setOrdersInfo(getAdminOrdersInfo(startCurrent, end, startPast, listOfProducts));
        adminSalesInformation.setSellersInfo(getAdminSellersInfo(startCurrent, end, startPast, listOfProducts));
        adminSalesInformation.setSuppliesInfo(getAdminSuppliesInfo(startCurrent, end, startPast, supplies));
        adminSalesInformation.setIncomeInfo(getAdminIncomeInfo(startCurrent, end, startPast, listOfProducts));

        return adminSalesInformation;
    }

    @Override
    public SellerSalesInformation getSellerSalesInformation(String keycloakId, DurationParams durationParams) {
        var duration = durationParams.getDuration();

        // filter work
        // startPast -------> startCurrent --------> end
        var end = LocalDate.now().plusDays(1).atStartOfDay();
        var startCurrent = end.minus(duration);
        var startPast = startCurrent.minus(duration);

        // todo: too long
        var orders = kaspiOrderRepository.findAllSellerOrders(keycloakId, getTimeStampFromLocalDateTime(startPast.minusDays(1)), getTimeStampFromLocalDateTime(end.plusDays(1)));
        var supplies = supplyRepository.findAllByCreatedAtBetweenAndAuthorKeycloakId(startPast, end, keycloakId);
        var supplyBoxProducts = supplyBoxProductsRepository.findAllSellerProductsInStore(keycloakId, startPast, end);

        log.info("startPast: {}, startCurrent: {}, end: {}, supplyBoxProducts size: {}, supplies size: {}, orders size: {}",
                startPast, startCurrent, end, supplyBoxProducts.size(), supplies.size(), orders.size());


        SellerSalesInformation sellerSalesInformation = new SellerSalesInformation();

        sellerSalesInformation.setOrdersInfo(getSellerOrdersInfo(orders, startCurrent, end, startPast));
        sellerSalesInformation.setSuppliesInfo(getSellerSuppliesInfo(supplies, startCurrent, end, startPast));
        sellerSalesInformation.setProductsInfo(getSellerProductsInfo(supplyBoxProducts, startCurrent, end, startPast));
        sellerSalesInformation.setIncomeInfo(getSellerIncomeInfo(supplyBoxProducts, startCurrent, end, startPast));

        return sellerSalesInformation;
    }

    @Override
    public Page<ProductWithCount> getSellerProductsCountInformation(String keycloakId, Pageable pageable) {
        var supplyBoxProducts = supplyBoxProductsRepository.findAllSellerProductsInStore(keycloakId, pageable);

        // pair of store id and product id as key of the map
        Map<Pair<Long, Long>, ProductWithCount> productWithCountMap = new HashMap<>();

        supplyBoxProducts.forEach(supplyBoxProduct -> {
            var product = supplyBoxProduct.getProduct();
            var store = supplyBoxProduct.getSupplyBox().getSupply().getKaspiStore();

            var key = Pair.of(store.getId(), product.getId());

            if (!productWithCountMap.containsKey(key)) {
                ProductWithCount productWithCount = new ProductWithCount();
                productWithCount.setName(product.getName());
                productWithCount.setArticle(product.getVendorCode());
                productWithCount.setStoreId(store.getId());
                productWithCount.setStoreFormattedAddress(store.getFormattedAddress());
                productWithCount.setCount(1L);
                productWithCountMap.put(key, productWithCount);
            } else {
                productWithCountMap.get(key).setCount(productWithCountMap.get(key).getCount() + 1);
            }
        });

        return new PageImpl<>(new ArrayList<>(productWithCountMap.values()), pageable, supplyBoxProducts.getTotalElements());
    }

    @Override
    public Page<AdminLastOrdersInformation> getAdminLastOrders(String keycloakId, Pageable pageable) {
        var end = LocalDateTime.now();
        var start = end.minusDays(7);

        var orders = kaspiOrderRepository.findAllAdminOrders(keycloakId, getTimeStampFromLocalDateTime(start), getTimeStampFromLocalDateTime(end), null, pageable);

        return orders.map(order -> {
            AdminLastOrdersInformation adminLastOrdersInformation = new AdminLastOrdersInformation();
            adminLastOrdersInformation.setOrderCode(order.getCode());
            adminLastOrdersInformation.setPrice(order.getTotalPrice());
            adminLastOrdersInformation.setShopName(order.getWonderUser().getKaspiToken().getSellerName());
            return adminLastOrdersInformation;
        });
    }

    @Override
    public Page<SellerTopProductInformation> getSellerTopProductsInformation(String keycloakId, Pageable pageable) {
        var kaspiOrderProducts = kaspiOrderProductRepository.findTopSellerProducts(keycloakId);

        Map<Long, SellerTopProductInformation> sellerTopProductInformationHashMap = new HashMap<>();

        kaspiOrderProducts.forEach(kaspiOrderProduct -> {
            var product = kaspiOrderProduct.getProduct();

            if (!sellerTopProductInformationHashMap.containsKey(product.getId())) {
                Optional<ProductPrice> productPrice = Optional.ofNullable(product.getMainCityPrice());
                if (productPrice.isEmpty()) {
                    productPrice = productPriceRepository.findFirstByProductIdOrderByPriceAsc(product.getId());
                }

                var sellerTopProductInformation = new SellerTopProductInformation();
                sellerTopProductInformation.setProductId(product.getId());
                sellerTopProductInformation.setProductName(product.getName());
                sellerTopProductInformation.setProductPrice(productPrice.orElse(new ProductPrice()).getPrice());
                sellerTopProductInformation.setCount(0L);
                sellerTopProductInformationHashMap.put(product.getId(), sellerTopProductInformation);
            }

            sellerTopProductInformationHashMap.get(product.getId()).setCount(sellerTopProductInformationHashMap.get(product.getId()).getCount() + kaspiOrderProduct.getQuantity());
        });

        var values = sellerTopProductInformationHashMap.values()
                .stream()
                .sorted((a, b) -> Math.toIntExact(a.getCount() - b.getCount()))
                .toList();


        return new PageImpl<>(values, pageable, kaspiOrderProducts.size());
    }

    @Override
    public Page<AdminTopSellerInformation> getAdminTopSellersInformation(String keycloakId, Pageable pageable) {
        var end = LocalDateTime.now();
        var start = end.minusDays(7);

        var orders = kaspiOrderRepository.findAllAdminOrders(keycloakId, getTimeStampFromLocalDateTime(start), getTimeStampFromLocalDateTime(end))
                .stream()
                .filter(order -> order.getProducts() != null && !order.getProducts().isEmpty())
                .toList();

        Map<Long, AdminTopSellerInformation> adminTopSellerInformationHashMap = new HashMap<>();


        orders
                .forEach(order -> {
                    var seller = order.getWonderUser();

                    adminTopSellerInformationHashMap.computeIfAbsent(seller.getId(), k -> {
                        AdminTopSellerInformation sellerTopSellerInformation = new AdminTopSellerInformation();
                        sellerTopSellerInformation.setShopName(seller.getKaspiToken().getSellerName());
                        sellerTopSellerInformation.setTotalIncome(0.);
                        return sellerTopSellerInformation;
                    });

                    adminTopSellerInformationHashMap.get(seller.getId()).setTotalIncome(order.getTotalPrice());
                });

        var values = adminTopSellerInformationHashMap.values()
                .stream()
                .sorted((a, b) -> (int) (a.getTotalIncome() - b.getTotalIncome()));

        return new PageImpl<>(values.toList(), pageable, orders.size());
    }

    @Override
    public List<DailyStats> getSellerDailyStats(String keycloakId, DurationParams durationParams) {
        var duration = durationParams.getDuration();

        var end = LocalDate.now().atStartOfDay();
        var start = end.minus(duration);

        var orders = kaspiOrderRepository.findAllSellerOrders(keycloakId, getTimeStampFromLocalDateTime(start), getTimeStampFromLocalDateTime(end));

        Map<String, DailyStats> dailyStatsMap = new TreeMap<>();
        LocalDateTime stepDate = start;

        while (!stepDate.isAfter(end) && !stepDate.isEqual(end)) {
            dailyStatsMap.put(stepDate.toString(), new DailyStats(stepDate.toString()));
            stepDate = nextStepDate(stepDate, durationParams);
        }

        orders.stream()
                .filter(order -> order.getProducts() != null && !order.getProducts().isEmpty())
                .forEach(order -> {
                    String orderDate = getOrderDateForDuration(Utils.getLocalDateTimeFromTimestamp(order.getCreationDate()), durationParams);
                    dailyStatsMap.computeIfPresent(orderDate, (k, v) -> {
                        v.addOrder(order);
                        return v;
                    });
                });

        return new ArrayList<>(dailyStatsMap.values());
    }

    @Override
    public List<DailyStats> getAdminDailyStats(String keycloakId, DurationParams durationParams) {
        var duration = durationParams.getDuration();

        var end = LocalDate.now().atStartOfDay();
        var start = end.minus(duration);

        var orders = kaspiOrderRepository.findAllAdminOrders(keycloakId, getTimeStampFromLocalDateTime(start), getTimeStampFromLocalDateTime(end));

        Map<String, DailyStats> dailyStatsMap = new TreeMap<>();
        LocalDateTime stepDate = start;

        while (!stepDate.isAfter(end) && !stepDate.isEqual(end)) {
            dailyStatsMap.put(stepDate.toString(), new DailyStats(stepDate.toString()));
            stepDate = nextStepDate(stepDate, durationParams);
        }

        orders.stream()
                .filter(order -> order.getProducts() != null && !order.getProducts().isEmpty())
                .forEach(order -> {
                    String orderDate = getOrderDateForDuration(Utils.getLocalDateTimeFromTimestamp(order.getCreationDate()), durationParams);
                    dailyStatsMap.computeIfPresent(orderDate, (k, v) -> {
                        v.addOrder(order);
                        return v;
                    });
                });

        return new ArrayList<>(dailyStatsMap.values());
    }

    private LocalDateTime nextStepDate(LocalDateTime currentDate, DurationParams duration) {
        return switch (duration) {
            case DAY -> currentDate.plusHours(2);
            case WEEK -> currentDate.plusDays(1);
            case MONTH -> currentDate.plusDays(3);
            case YEAR -> currentDate.plusMonths(1);
        };
    }

    private String getOrderDateForDuration(LocalDateTime orderDate, DurationParams duration) {
        return switch (duration) {
            case DAY -> String.valueOf(orderDate.getHour() - (orderDate.getHour() % 2));
            case WEEK -> orderDate.getDayOfWeek().toString();
            case MONTH -> Month.of(orderDate.getDayOfMonth()).toString();
            case YEAR -> orderDate.withDayOfYear(1).toLocalDate().toString();
        };
    }

    private StateInfo<Double> getSellerIncomeInfo(List<SupplyBoxProduct> supplyBoxProducts, LocalDateTime startCurrent, LocalDateTime end, LocalDateTime startPast) {
        StateInfo<Double> incomeInfo = new StateInfo<>();

        AtomicReference<Double> countOfCurrent = new AtomicReference<>(0.);
        AtomicReference<Double> countOfPast = new AtomicReference<>(0.);

        supplyBoxProducts.forEach(spb -> {
            var kaspiOrder = spb.getKaspiOrder();
            if (kaspiOrder == null)
                return;

            var creationDate = Utils.getLocalDateTimeFromTimestamp(kaspiOrder.getCreationDate());
            if (isCurrent(startCurrent, end, creationDate)) {
                countOfCurrent.getAndSet(countOfCurrent.get() + kaspiOrder.getTotalPrice());
            } else if (isPast(startPast, end, creationDate)) {
                countOfPast.getAndSet(countOfPast.get() + kaspiOrder.getTotalPrice());
            }
        });

        incomeInfo.setCount(countOfCurrent.get());

        if (countOfPast.get() == 0 || (countOfPast.get().equals(countOfCurrent.get()))) {
            incomeInfo.setPercent(null);
        } else if (countOfCurrent.get() == 0) {
            incomeInfo.setPercent(-100.0);
        } else {
            incomeInfo.setPercent(getPercent(countOfCurrent.get(), countOfPast.get()));
        }

        return incomeInfo;
    }

    private StateInfo<Long> getSellerOrdersInfo(List<KaspiOrder> orders, LocalDateTime startCurrent, LocalDateTime end, LocalDateTime startPast) {
        StateInfo<Long> orderInfo = new StateInfo<>();

        AtomicReference<Long> countOfCurrent = new AtomicReference<>(0L);
        AtomicReference<Long> countOPast = new AtomicReference<>(0L);

        orders.forEach(kaspiOrder -> {
            var creationDate = Utils.getLocalDateTimeFromTimestamp(kaspiOrder.getCreationDate());
            if (isCurrent(startCurrent, end, creationDate)) {
                countOfCurrent.getAndSet(countOfCurrent.get() + 1);
            } else if (isPast(startPast, end, creationDate)) {
                countOPast.getAndSet(countOPast.get() + 1);
            }
        });

        orderInfo.setCount(countOfCurrent.get());

        if (countOPast.get() == 0 || (countOPast.get().equals(countOfCurrent.get()))) {
            orderInfo.setPercent(null);
        } else if (countOfCurrent.get() == 0) {
            orderInfo.setPercent(-100.0);
        } else {
            orderInfo.setPercent(getPercent(countOfCurrent.get(), countOPast.get()));
        }

        log.info("orders in past: {}, orders in current: {}", countOPast.get(), countOfCurrent.get());

        return orderInfo;
    }

    private StateInfo<Long> getSellerSuppliesInfo(List<Supply> supplies, LocalDateTime startCurrent, LocalDateTime end, LocalDateTime startPast) {
        StateInfo<Long> suppliesInfo = new StateInfo<>();

        AtomicReference<Long> countOfCurrent = new AtomicReference<>(0L);
        AtomicReference<Long> countOfPast = new AtomicReference<>(0L);

        supplies.forEach(kaspiOrder -> {
            var creationDate = kaspiOrder.getCreatedAt();
            if (isCurrent(startCurrent, end, creationDate)) {
                countOfCurrent.getAndSet(countOfCurrent.get() + 1);
            } else if (isPast(startPast, end, creationDate)) {
                countOfPast.getAndSet(countOfPast.get() + 1);
            }
        });

        suppliesInfo.setCount(countOfCurrent.get());

        if (countOfPast.get() == 0 || (countOfPast.get().equals(countOfCurrent.get()))) {
            suppliesInfo.setPercent(null);
        } else if (countOfCurrent.get() == 0) {
            suppliesInfo.setPercent(-100.0);
        } else {
            suppliesInfo.setPercent(getPercent(countOfCurrent.get(), countOfPast.get()));
        }

        return suppliesInfo;
    }

    private StateInfo<Long> getSellerProductsInfo(List<SupplyBoxProduct> supplyBoxProducts, LocalDateTime startCurrent, LocalDateTime end, LocalDateTime startPast) {
        StateInfo<Long> productsInfo = new StateInfo<>();

        AtomicReference<Long> countOfCurrent = new AtomicReference<>(0L);
        AtomicReference<Long> countOfPast = new AtomicReference<>(0L);

        supplyBoxProducts.forEach(supplyBoxProduct -> {
            var creationDate = supplyBoxProduct.getCreatedAt();
            if (isCurrent(startCurrent, end, creationDate)) {
                countOfCurrent.getAndSet(countOfCurrent.get() + 1);
            } else if (isPast(startPast, end, creationDate)) {
                countOfPast.getAndSet(countOfPast.get() + 1);
            }
        });

        productsInfo.setCount(countOfCurrent.get());

        if (countOfPast.get() == 0 || (countOfPast.get().equals(countOfCurrent.get()))) {
            productsInfo.setPercent(null);
        } else if (countOfCurrent.get() == 0) {
            productsInfo.setPercent(-100.0);
        } else {
            productsInfo.setPercent(getPercent(countOfCurrent.get(), countOfPast.get()));
        }

        return productsInfo;
    }

    // todo: make it with one cycle
    private StateInfo<Double> getAdminIncomeInfo(LocalDateTime startCurrent, LocalDateTime end, LocalDateTime startPast, List<SupplyBoxProduct> listOfProducts) {
        StateInfo<Double> incomeInfo = new StateInfo<>();

        AtomicReference<Double> countOfCurrent = new AtomicReference<>(0.);
        AtomicReference<Double> countOfPast = new AtomicReference<>(0.);

        listOfProducts.forEach(spb -> {
            var kaspiOrder = spb.getKaspiOrder();
            var creationDate = Utils.getLocalDateTimeFromTimestamp(kaspiOrder.getCreationDate());
            if (isCurrent(startCurrent, end, creationDate)) {
                countOfCurrent.getAndSet(countOfCurrent.get() + kaspiOrder.getTotalPrice());
            } else if (isPast(startPast, end, creationDate)) {
                countOfPast.getAndSet(countOfPast.get() + kaspiOrder.getTotalPrice());
            }
        });

        incomeInfo.setCount(countOfCurrent.get());

        if (countOfPast.get() == 0 || (countOfPast.get().equals(countOfCurrent.get()))) {
            incomeInfo.setPercent(null);
        } else if (countOfCurrent.get() == 0) {
            incomeInfo.setPercent(-100.0);
        } else {
            incomeInfo.setPercent(getPercent(countOfCurrent.get(), countOfPast.get()));
        }

        return incomeInfo;
    }

    private StateInfo<Long> getAdminSuppliesInfo(LocalDateTime startCurrent, LocalDateTime end, LocalDateTime startPast, List<Supply> supplies) {
        StateInfo<Long> suppliesInfo = new StateInfo<>();
        AtomicReference<Long> countOfCurrent = new AtomicReference<>((long) 0);
        AtomicReference<Long> countOfPast = new AtomicReference<>((long) 0);


        supplies.forEach(supply -> {
            var creationDate = supply.getCreatedAt();

            if (isCurrent(startCurrent, end, creationDate)) {
                countOfCurrent.getAndSet(countOfCurrent.get() + 1);
            } else if (isPast(startPast, end, creationDate)) {
                countOfPast.getAndSet(countOfPast.get() + 1);
            }
        });

        suppliesInfo.setCount(countOfCurrent.get());


        if (countOfPast.get() == 0 || (countOfPast.get().equals(countOfCurrent.get()))) {
            suppliesInfo.setPercent(null);
        } else if (countOfCurrent.get() == 0) {
            suppliesInfo.setPercent(-100.0);
            suppliesInfo.setPercent(getPercent(countOfCurrent.get(), countOfPast.get()));
        }

        return suppliesInfo;
    }


    private StateInfo<Integer> getAdminSellersInfo(LocalDateTime startCurrent, LocalDateTime end, LocalDateTime startPast, List<SupplyBoxProduct> listOfProducts) {
        StateInfo<Integer> sellerInfo = new StateInfo<>();

        Set<Long> sellerMapCurrent = new HashSet<>();
        Set<Long> sellerMapPast = new HashSet<>();

        listOfProducts.forEach(spb -> {
            var kaspiOrder = spb.getKaspiOrder();
            var creationDate = Utils.getLocalDateTimeFromTimestamp(kaspiOrder.getCreationDate());
            if (isCurrent(startCurrent, end, creationDate)) {
                sellerMapCurrent.add(kaspiOrder.getWonderUser().getId());
            } else if (isPast(startPast, end, creationDate)) {
                sellerMapPast.add(kaspiOrder.getWonderUser().getId());
            }
        });

        sellerInfo.setCount(sellerMapCurrent.size());

        if (sellerMapPast.isEmpty() || (sellerMapPast.size() == sellerMapCurrent.size())) {
            sellerInfo.setPercent(null);
        } else if (sellerMapCurrent.isEmpty()) {
            sellerInfo.setPercent(-100.0);
        } else {
            sellerInfo.setPercent(getPercent(sellerMapCurrent.size(), sellerMapPast.size()));

        }

        return sellerInfo;
    }

    private StateInfo<Long> getAdminOrdersInfo(LocalDateTime startCurrent, LocalDateTime end, LocalDateTime startPast, List<SupplyBoxProduct> listOfProducts) {
        StateInfo<Long> ordersInfo = new StateInfo<>();
        var countOfCurrent = listOfProducts.stream().filter(i -> {
            var kaspiOrder = i.getKaspiOrder();
            var creationDate = Utils.getLocalDateTimeFromTimestamp(kaspiOrder.getCreationDate());
            return isCurrent(startCurrent, end, creationDate);
        }).count();

        ordersInfo.setCount(countOfCurrent);

        var countOfPast = listOfProducts.stream().filter(i -> {
            var kaspiOrder = i.getKaspiOrder();
            var creationDate = Utils.getLocalDateTimeFromTimestamp(kaspiOrder.getCreationDate());
            return isPast(startPast, startCurrent, creationDate);
        }).count();

        if (countOfPast == 0 || (countOfPast == countOfCurrent)) {
            ordersInfo.setPercent(null);
        } else if (countOfCurrent == 0) {
            ordersInfo.setPercent(-100.0);
        } else {
            ordersInfo.setPercent(getPercent(countOfCurrent, countOfPast));
        }

        return ordersInfo;
    }

    private boolean isCurrent(LocalDateTime startCurrent, LocalDateTime end, LocalDateTime creationDate) {
        return ((creationDate.isEqual(startCurrent) || creationDate.isAfter(startCurrent)) && creationDate.isBefore(end));
    }

    private boolean isPast(LocalDateTime startPast, LocalDateTime startCurrent, LocalDateTime creationDate) {
        return ((creationDate.isEqual(startPast) || creationDate.isAfter(startPast)) && creationDate.isBefore(startCurrent));
    }


    private double getPercent(double countOfCurrent, double countOfPast) {
        var percent = calculatePercent(Math.max(countOfCurrent, countOfPast), Math.min(countOfCurrent, countOfPast));
        return Double.parseDouble(DECIMAL_FORMAT.format((percent * (countOfPast > countOfCurrent ? -1.0 : 1.0)) % 100));
    }

    private Double calculatePercent(Double max, Double min) {
        return (max / min) * 100;
    }

}
