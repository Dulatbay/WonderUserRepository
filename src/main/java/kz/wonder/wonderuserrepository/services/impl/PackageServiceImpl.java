package kz.wonder.wonderuserrepository.services.impl;

import kz.wonder.wonderuserrepository.dto.response.StartPackageResponse;
import kz.wonder.wonderuserrepository.entities.KaspiOrder;
import kz.wonder.wonderuserrepository.entities.OrderPackage;
import kz.wonder.wonderuserrepository.entities.OrderPackageProcess;
import kz.wonder.wonderuserrepository.entities.StoreEmployee;
import kz.wonder.wonderuserrepository.entities.enums.AssembleState;
import kz.wonder.wonderuserrepository.entities.enums.PackageState;
import kz.wonder.wonderuserrepository.entities.enums.ProductStateInStore;
import kz.wonder.wonderuserrepository.exceptions.DbObjectNotFoundException;
import kz.wonder.wonderuserrepository.repositories.*;
import kz.wonder.wonderuserrepository.services.PackageService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.ws.rs.NotAuthorizedException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static kz.wonder.wonderuserrepository.constants.ValueConstants.ZONE_ID;

@Service
@RequiredArgsConstructor
public class PackageServiceImpl implements PackageService {
    private final OrderPackageProcessRepository orderPackageProcessRepository;
    private final OrderPackageRepository orderPackageRepository;
    private final StoreEmployeeRepository storeEmployeeRepository;
    private final SupplyBoxProductsRepository supplyBoxProductsRepository;
    private final KaspiOrderRepository kaspiOrderRepository;
    private final MessageSource messageSource;

    @Override
    public StartPackageResponse startPackaging(String orderCode, String keycloakId) {
        var storeEmployee = storeEmployeeRepository.findByWonderUserKeycloakId(keycloakId)
                .orElseThrow(() -> new NotAuthorizedException(""));

        var order = kaspiOrderRepository.findByCode(orderCode)
                .orElseThrow(() -> new IllegalArgumentException("Заказ не найден"));

        validateEmployeeWithStore(storeEmployee, order);

        var orderAssemble = order.getOrderAssemble();
        var packageOrder = order.getOrderPackage();

        if (orderAssemble == null || orderAssemble.getAssembleState() != AssembleState.FINISHED) {
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
        return new StartPackageResponse(this.getWaybill(order));
    }

    @Override
    @Transactional
    public void packageProductStart(String orderCode, String productArticle, String keycloakId) {
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

        var supplyBoxProduct = supplyBoxProductsRepository.findByArticleAndOrderCode(productArticle, order.getCode())
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase(), "Неверный артикул"));

        if (supplyBoxProduct.getState() != ProductStateInStore.PACKING) {
            throw new IllegalArgumentException("Продукт уже в процессе упаковки");
        }

        if (supplyBoxProduct.getState() != ProductStateInStore.READY_FOR_PACKAGE) {
            throw new IllegalArgumentException("Продукт не готов к упаковке");
        }


        supplyBoxProduct.setState(ProductStateInStore.PACKING);
        supplyBoxProductsRepository.save(supplyBoxProduct);

        var orderPackageProcess = orderPackageProcessRepository.findBySupplyBoxProductId(supplyBoxProduct.getId())
                .orElse(new OrderPackageProcess());

        orderPackageProcess.setOrderPackage(orderPackage);
        orderPackageProcess.setEmployee(storeEmployee);
        orderPackageProcess.setSupplyBoxProduct(supplyBoxProduct);
        orderPackageProcess.setStartedAt(LocalDateTime.now(ZONE_ID));
        orderPackageProcessRepository.save(orderPackageProcess);
    }


    @Override
    public void packageProductFinish(String orderCode, String productArticle, String keycloakId) {
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

        var supplyBoxProduct = supplyBoxProductsRepository.findByArticleAndOrderCode(productArticle, order.getCode())
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase(), "Неверный артикул"));

        if(supplyBoxProduct.getState() != ProductStateInStore.PACKING)
            throw new IllegalArgumentException("Невозвожно завершить упаковку");

        var orderPackageProcess = orderPackageProcessRepository.findBySupplyBoxProductId(supplyBoxProduct.getId())
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase(), "Невозможно найти процесс упаковки"));

        orderPackageProcess.setFinishedAt(LocalDateTime.now(ZONE_ID));
        orderPackageProcessRepository.save(orderPackageProcess);

        supplyBoxProduct.setState(ProductStateInStore.PACKED);
        supplyBoxProductsRepository.save(supplyBoxProduct);

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

    private void validateEmployeeWithStore(StoreEmployee storeEmployee, KaspiOrder order) {
        var storeEmployeeKaspiStore = storeEmployee.getKaspiStore();
        var orderStore = order.getKaspiStore();

        if (!storeEmployeeKaspiStore.getId().equals(orderStore.getId()))
            throw new IllegalArgumentException("Заказ не найден");

        var orderProducts = order.getProducts();

        if (orderProducts == null || orderProducts.isEmpty())
            throw new IllegalArgumentException("Заказ не может быть собран");

    }

    private String getWaybill(KaspiOrder kaspiOrder) {
        if (kaspiOrder.getWaybill() != null) return kaspiOrder.getWaybill();
        return messageSource.getMessage("services-impl.assembly-service-impl.generated-soon", null, LocaleContextHolder.getLocale());
    }
}
