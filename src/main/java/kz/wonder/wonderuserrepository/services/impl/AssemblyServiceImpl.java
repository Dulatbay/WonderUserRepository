package kz.wonder.wonderuserrepository.services.impl;

import jakarta.transaction.Transactional;
import kz.wonder.wonderuserrepository.constants.Utils;
import kz.wonder.wonderuserrepository.dto.params.AssemblySearchParameters;
import kz.wonder.wonderuserrepository.dto.request.AssembleProductRequest;
import kz.wonder.wonderuserrepository.dto.response.AssembleProcessResponse;
import kz.wonder.wonderuserrepository.dto.response.EmployeeAssemblyResponse;
import kz.wonder.wonderuserrepository.entities.*;
import kz.wonder.wonderuserrepository.entities.enums.AssembleState;
import kz.wonder.wonderuserrepository.entities.enums.ProductStateInStore;
import kz.wonder.wonderuserrepository.exceptions.DbObjectNotFoundException;
import kz.wonder.wonderuserrepository.mappers.OrderAssembleMapper;
import kz.wonder.wonderuserrepository.repositories.*;
import kz.wonder.wonderuserrepository.services.AssemblyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import javax.ws.rs.NotAuthorizedException;

@Service
@Slf4j
@RequiredArgsConstructor
public class AssemblyServiceImpl implements AssemblyService {
    private final SupplyBoxProductsRepository supplyBoxProductsRepository;
    private final KaspiOrderRepository kaspiOrderRepository;
    private final StoreEmployeeRepository storeEmployeeRepository;
    private final OrderAssembleRepository orderAssembleRepository;
    private final OrderAssembleMapper orderAssembleMapper;
    private final StoreCellProductRepository storeCellProductRepository;
    private final OrderAssembleProcessRepository orderAssembleProcessRepository;

    private static @NotNull OrderAssemble validateAssembleToFinish(KaspiOrder order) {
        var assemble = order.getOrderAssemble();

        if (assemble == null) {
            throw new IllegalArgumentException("Состояние сборки не готово к завершению");
        }

        if (assemble.getAssembleState() == AssembleState.FINISHED) {
            throw new IllegalArgumentException("Состояние сборки уже завершено");
        } else if (assemble.getAssembleState() != AssembleState.READY_TO_FINISH) {
            throw new IllegalArgumentException("Состояние сборки не готово к завершению");
        }
        return assemble;
    }

    @Override
    public Page<EmployeeAssemblyResponse> findAssembliesByParams(String keycloakId, AssemblySearchParameters assemblySearchParameters) {
        long startUnixTimestamp = Utils.getTimeStampFromLocalDateTime(assemblySearchParameters.getOrderCreationStartDate().atStartOfDay());
        long endUnixTimestamp = Utils.getTimeStampFromLocalDateTime(assemblySearchParameters.getOrderCreationEndDate().atStartOfDay());

        PageRequest pageRequest = PageRequest.of(assemblySearchParameters.getPage(), assemblySearchParameters.getSize(), Sort.by(assemblySearchParameters.getSortBy()));
        String productState = assemblySearchParameters.getProductStateInStore() == null ? null : assemblySearchParameters.getProductStateInStore().name();
        String deliveryMode = assemblySearchParameters.getDeliveryMode() == null ? null : assemblySearchParameters.getDeliveryMode().name();

        log.info("Search assemblies, start unix timestamp: {}, endUnixTimeStamp: {}, product state: {}, delivery mode: {}", startUnixTimestamp, endUnixTimestamp, productState, deliveryMode);
        var supplyBoxProducts = supplyBoxProductsRepository.findAllEmployeeAssemblies(startUnixTimestamp, endUnixTimestamp, productState, deliveryMode, keycloakId, pageRequest);


        return supplyBoxProducts.map(supplyBoxProduct -> {
            var orderAssemble = orderAssembleRepository.findByKaspiOrderId(supplyBoxProduct.getKaspiOrder().getId());
            return orderAssembleMapper.mapToEmployeeAssemblyResponse(supplyBoxProduct, orderAssemble.map(OrderAssemble::getAssembleState).orElse(AssembleState.WAITING_TO_ASSEMBLE));
        });
    }

    @Override
    public AssembleProcessResponse startAssemble(JwtAuthenticationToken starterToken, String orderCode) {
        var storeEmployee = storeEmployeeRepository.findByWonderUserKeycloakId(Utils.extractIdFromToken(starterToken))
                .orElseThrow(() -> new NotAuthorizedException(""));

        var order = kaspiOrderRepository.findByCode(orderCode)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND, HttpStatus.NOT_FOUND.getReasonPhrase(), "Заказ не найден"));

        validateEmployeeWithStore(storeEmployee, order);

        var orderAssembleOptional = orderAssembleRepository.findByKaspiOrderId(order.getId());

        if (orderAssembleOptional.isPresent()) {
            throw new IllegalArgumentException("Сборка уже началась");
        }

        var orderAssemble = orderAssembleRepository.save(orderAssembleMapper.toEntity(storeEmployee, order, AssembleState.STARTED));

        var dividedProducts = orderAssembleMapper.divideProducts(order);

        return orderAssembleMapper.toProcessResponse(order, Utils.extractNameFromToken(starterToken), orderAssemble, dividedProducts.getLeft(), dividedProducts.getRight());
    }

    @Override
    public AssembleProcessResponse assembleProduct(JwtAuthenticationToken starterToken, AssembleProductRequest assembleProductRequest) {
        var storeEmployee = storeEmployeeRepository.findByWonderUserKeycloakId(Utils.extractIdFromToken(starterToken))
                .orElseThrow(() -> new NotAuthorizedException(""));

        var order = kaspiOrderRepository.findByCode(assembleProductRequest.getOrderCode())
                .orElseThrow(() -> new IllegalArgumentException("Заказ не найден"));

        var store = validateEmployeeWithStore(storeEmployee, order);

        var assemble = order.getOrderAssemble();

        if (assemble == null) {
            throw new IllegalArgumentException("Сборка еще не началась");
        }

        if (assemble.getAssembleState() == AssembleState.FINISHED) {
            throw new IllegalArgumentException("Сборка уже закончена");
        }

        assembleProductRequest.getProductArticles()
                .forEach(productArticle -> {
                    var supplyBoxProduct = supplyBoxProductsRepository.findByArticleAndStore(productArticle, store.getId())
                            .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase(), "Incorrect article"));

                    var storeCellProduct = supplyBoxProduct.getStoreCellProduct();

                    supplyBoxProduct.setState(ProductStateInStore.ASSEMBLED);
                    supplyBoxProductsRepository.save(supplyBoxProduct);


                    storeCellProduct.setBusy(false);
                    storeCellProductRepository.save(storeCellProduct);

                    var orderAssembleProcessOptional = orderAssembleProcessRepository.findByOrderAssembleIdAndStoreCellProductId(assemble.getId(), storeCellProduct.getId());

                    if (orderAssembleProcessOptional.isEmpty()) {
                        orderAssembleProcessRepository.save(orderAssembleMapper.toOrderAssembleProcessEntity(order, storeEmployee, storeCellProduct));
                    } else {
                        var orderAssembleProcess = orderAssembleProcessOptional.get();
                        orderAssembleProcess.setStoreEmployee(storeEmployee);
                        orderAssembleProcessRepository.save(orderAssembleProcess);
                    }
                });


        var dividedProducts = orderAssembleMapper.divideProducts(order);

        if (dividedProducts.getLeft().isEmpty()) {
            assemble.setAssembleState(AssembleState.READY_TO_FINISH);
        } else {
            assemble.setAssembleState(AssembleState.IN_PROGRESS);
        }

        orderAssembleRepository.save(assemble);

        return orderAssembleMapper.toProcessResponse(order, storeEmployee.getWonderUser().getUsername(), assemble, dividedProducts.getLeft(), dividedProducts.getRight());
    }

    @Override
    public AssembleProcessResponse getAssemble(JwtAuthenticationToken starterToken, String orderCode) {
        var storeEmployee = storeEmployeeRepository.findByWonderUserKeycloakId(Utils.extractIdFromToken(starterToken))
                .orElseThrow(() -> new NotAuthorizedException(""));

        var order = kaspiOrderRepository.findByCode(orderCode)
                .orElseThrow(() -> new IllegalArgumentException("Заказ не найден"));

        validateEmployeeWithStore(storeEmployee, order);

        var orderAssemble = order.getOrderAssemble();
        String starterName = null;


        if (orderAssemble == null) {
            orderAssemble = new OrderAssemble();
        } else {
            starterName = orderAssemble.getStartedEmployee().getWonderUser().getUsername();
        }

        var dividedProducts = orderAssembleMapper.divideProducts(order);


        return orderAssembleMapper.toProcessResponse(order,
                starterName,
                orderAssemble,
                dividedProducts.getLeft(),
                dividedProducts.getRight());

    }

    @Override
    @Transactional
    public void finishAssemble(String orderCode, String keycloakId) {
        var storeEmployee = storeEmployeeRepository.findByWonderUserKeycloakId(keycloakId)
                .orElseThrow(() -> new NotAuthorizedException(""));

        var order = kaspiOrderRepository.findByCode(orderCode)
                .orElseThrow(() -> new IllegalArgumentException("Заказ не найден"));

        var assemble = validateAssembleToFinish(order);

        validateEmployeeWithStore(storeEmployee, order);

        var dividedProducts = orderAssembleMapper.divideProducts(order);

        if (!dividedProducts.getLeft().isEmpty()) {
            throw new IllegalArgumentException(dividedProducts.getLeft().size() + " товаров осталось для сканирования");
        }

        assemble.setAssembleState(AssembleState.FINISHED);
        orderAssembleRepository.save(assemble);

        order.getProducts()
                .forEach(kaspiOrderProduct -> {
                    var sbp = kaspiOrderProduct.getSupplyBoxProduct();
                    sbp.setState(ProductStateInStore.READY_FOR_PACKAGE);
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

    private String getWaybill(KaspiOrder kaspiOrder) {
        if (kaspiOrder.getWaybill() != null) return kaspiOrder.getWaybill();
        // generate with api
        return "Сгениророван(Скоро)";
    }
}
