package kz.wonder.wonderuserrepository.services.impl;

import kz.wonder.wonderuserrepository.constants.Utils;
import kz.wonder.wonderuserrepository.dto.params.DurationParams;
import kz.wonder.wonderuserrepository.dto.response.AdminSalesInformation;
import kz.wonder.wonderuserrepository.dto.response.SellerSalesInformation;
import kz.wonder.wonderuserrepository.dto.response.StateInfo;
import kz.wonder.wonderuserrepository.entities.KaspiOrder;
import kz.wonder.wonderuserrepository.entities.Supply;
import kz.wonder.wonderuserrepository.entities.SupplyBoxProduct;
import kz.wonder.wonderuserrepository.repositories.KaspiOrderRepository;
import kz.wonder.wonderuserrepository.repositories.SupplyBoxProductsRepository;
import kz.wonder.wonderuserrepository.repositories.SupplyRepository;
import kz.wonder.wonderuserrepository.services.StatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static kz.wonder.wonderuserrepository.constants.ValueConstants.DECIMAL_FORMAT;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {
    private final SupplyBoxProductsRepository supplyBoxProductsRepository;
    private final SupplyRepository supplyRepository;
    private final KaspiOrderRepository kaspiOrderRepository;

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
        var orders = kaspiOrderRepository.findAllBySeller(keycloakId, Utils.getTimeStampFromLocalDateTime(startPast.minusDays(1)), Utils.getTimeStampFromLocalDateTime(end.plusDays(1)));
        var supplies = supplyRepository.findAllByCreatedAtBetweenAndAuthorKeycloakId(startPast, end, keycloakId);
        var supplyBoxProducts = supplyBoxProductsRepository.findAllSellerProducts(keycloakId, startPast, end);

        log.info("startPast: {}, startCurrent: {}, end: {}, supplyBoxProducts size: {}, supplies size: {}, orders size: {}",
                startPast, startCurrent, end, supplyBoxProducts.size(), supplies.size(), orders.size());


        SellerSalesInformation sellerSalesInformation = new SellerSalesInformation();

        sellerSalesInformation.setOrdersInfo(getSellerOrdersInfo(orders, startCurrent, end, startPast));
        sellerSalesInformation.setSuppliesInfo(getSellerSuppliesInfo(supplies, startCurrent, end, startPast));
        sellerSalesInformation.setProductsInfo(getSellerProductsInfo(supplyBoxProducts, startCurrent, end, startPast));
        sellerSalesInformation.setIncomeInfo(getSellerIncomeInfo(supplyBoxProducts, startCurrent, end, startPast));

        return sellerSalesInformation;
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