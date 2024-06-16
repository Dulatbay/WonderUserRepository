package kz.wonder.wonderuserrepository.services.impl;

import kz.wonder.wonderuserrepository.dto.request.PackageProductRequest;
import kz.wonder.wonderuserrepository.entities.*;
import kz.wonder.wonderuserrepository.entities.enums.AssembleState;
import kz.wonder.wonderuserrepository.entities.enums.PackageState;
import kz.wonder.wonderuserrepository.entities.enums.ProductStateInStore;
import kz.wonder.wonderuserrepository.exceptions.DbObjectNotFoundException;
import kz.wonder.wonderuserrepository.repositories.*;
import kz.wonder.wonderuserrepository.services.PackageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.ws.rs.NotAuthorizedException;

@Service
@RequiredArgsConstructor
public class PackageServiceImpl implements PackageService {
    private final OrderPackageProcessRepository orderPackageProcessRepository;
    private final OrderPackageRepository orderPackageRepository;
    private final OrderAssembleRepository orderAssembleRepository;
    private final StoreEmployeeRepository storeEmployeeRepository;
    private final SupplyBoxProductsRepository supplyBoxProductsRepository;
    private final KaspiOrderRepository kaspiOrderRepository;


    @Override
    public void startPackaging(String orderCode, String keycloakId) {
        var storeEmployee = storeEmployeeRepository.findByWonderUserKeycloakId(keycloakId)
                .orElseThrow(() -> new NotAuthorizedException(""));

        var order = kaspiOrderRepository.findByCode(orderCode)
                .orElseThrow(() -> new IllegalArgumentException("Заказ не найден"));

        validateEmployeeWithStore(storeEmployee, order);

        var orderAssemble = order.getOrderAssemble();
        var packageOrder = order.getOrderPackage();

        if(orderAssemble == null || orderAssemble.getAssembleState() != AssembleState.FINISHED){
            throw new IllegalArgumentException("Заказ не собран");
        }

        if (packageOrder != null) {
            throw new IllegalArgumentException("Упаковка уже началась");
        }

        OrderPackage orderPackage = new OrderPackage();
        orderPackage.setPackageState(PackageState.STARTED);
        orderPackage.setKaspiOrder(order);
        orderPackage.setStartedEmployee(storeEmployee);

        orderPackageRepository.save(orderPackage);
    }

    @Override
    @Transactional
    public void packageProduct(String orderCode, PackageProductRequest packageProductRequest, String keycloakId) {
        var storeEmployee = storeEmployeeRepository.findByWonderUserKeycloakId(keycloakId)
                .orElseThrow(() -> new NotAuthorizedException(""));

        var order = kaspiOrderRepository.findByCode(orderCode)
                .orElseThrow(() -> new IllegalArgumentException("Заказ не найден"));

        validateEmployeeWithStore(storeEmployee, order);

        var orderPackage = order.getOrderPackage();

        if (orderPackage == null) {
            throw new IllegalArgumentException("Упаковка еще не началась");
        }

        if (orderPackage.getPackageState() == PackageState.FINISHED) {
            throw new IllegalArgumentException("Упаковка уже закончена");
        }

        packageProductRequest.getProductArticles()
                .forEach(article -> {
                    var supplyBoxProduct = supplyBoxProductsRepository.findByArticleAndOrderCode(article, order.getCode())
                            .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase(), "Неверный артикул"));


                    supplyBoxProduct.setState(ProductStateInStore.PACKED);
                    supplyBoxProductsRepository.save(supplyBoxProduct);

                    var orderPackageProcess = orderPackageProcessRepository.findBySupplyBoxProductId(supplyBoxProduct.getId())
                            .orElse(new OrderPackageProcess());

                    orderPackageProcess.setOrderPackage(orderPackage);
                    orderPackageProcess.setEmployee(storeEmployee);
                    orderPackageProcess.setSupplyBoxProduct(supplyBoxProduct);
                    orderPackageProcessRepository.save(orderPackageProcess);
                });

        boolean isReadyToFinish = !order.getProducts()
                .stream()
                .anyMatch(k -> k.getSupplyBoxProduct().getState() != ProductStateInStore.PACKED);

        if (isReadyToFinish) {
            orderPackage.setPackageState(PackageState.READY_TO_FINISH);
            orderPackageRepository.save(orderPackage);
        }
    }

    @Override
    @Transactional
    public void finishPackaging(String orderCode, String keycloakId) {
        var storeEmployee = storeEmployeeRepository.findByWonderUserKeycloakId(keycloakId)
                .orElseThrow(() -> new NotAuthorizedException(""));

        var order = kaspiOrderRepository.findByCode(orderCode)
                .orElseThrow(() -> new IllegalArgumentException("Заказ не найден"));

        validateEmployeeWithStore(storeEmployee, order);

        var orderPackage = order.getOrderPackage();

        if (orderPackage == null) {
            throw new IllegalArgumentException("Упаковка еще не началась");
        }

        if (orderPackage.getPackageState() == PackageState.FINISHED) {
            throw new IllegalArgumentException("Упаковка уже закончена");
        }

        if (orderPackage.getPackageState() != PackageState.READY_TO_FINISH) {
            throw new IllegalArgumentException("Упаковка не готова к завершению");
        }

        var productsLeftToPack = order.getProducts()
                .stream()
                .filter(p -> p.getSupplyBoxProduct().getState() != ProductStateInStore.PACKED)
                .count();

        if (productsLeftToPack > 0) {
            throw new IllegalArgumentException(productsLeftToPack + " товаров осталось для сканирования");
        }

        orderPackage.setStartedEmployee(storeEmployee);
        orderPackage.setPackageState(PackageState.FINISHED);
        orderPackageRepository.save(orderPackage);
        // todo: make also for storeEmployee that will finish packing
        order.getProducts()
                .forEach(kaspiOrderProduct -> {
                    var sbp = kaspiOrderProduct.getSupplyBoxProduct();
                    sbp.setState(ProductStateInStore.READY_TO_SENDING);
                    supplyBoxProductsRepository.save(sbp);
                });

    }

    private KaspiStore validateEmployeeWithStore(StoreEmployee storeEmployee, KaspiOrder order) {
        var storeEmployeeKaspiStore = storeEmployee.getKaspiStore();
        var orderStore = order.getKaspiStore();

        if (!storeEmployeeKaspiStore.getId().equals(orderStore.getId()))
            throw new IllegalArgumentException("Заказ не найден");

        var orderProducts = order.getProducts();

        if (orderProducts == null || orderProducts.isEmpty())
            throw new IllegalArgumentException("Заказ не может быть собран");

        return orderStore;
    }
}
