package kz.wonder.wonderuserrepository.services.impl;

import jakarta.transaction.Transactional;
import kz.wonder.wonderuserrepository.constants.Utils;
import kz.wonder.wonderuserrepository.dto.params.AssemblySearchParameters;
import kz.wonder.wonderuserrepository.dto.response.AssembleProcessResponse;
import kz.wonder.wonderuserrepository.dto.response.AssembleProductResponse;
import kz.wonder.wonderuserrepository.dto.response.EmployeeAssemblyResponse;
import kz.wonder.wonderuserrepository.entities.*;
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

import static kz.wonder.wonderuserrepository.constants.ValueConstants.ZONE_ID;

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


    @Override
    public Page<EmployeeAssemblyResponse> findAssembliesByParams(String keycloakId, AssemblySearchParameters assemblySearchParameters) {
        long startUnixTimestamp = assemblySearchParameters.getOrderCreationStartDate().atStartOfDay().atZone(ZONE_ID).toInstant().getEpochSecond() * 1000;
        long endUnixTimestamp = assemblySearchParameters.getOrderCreationEndDate().atStartOfDay().plusDays(1).atZone(ZONE_ID).toInstant().getEpochSecond() * 1000;

        PageRequest pageRequest = PageRequest.of(assemblySearchParameters.getPage(), assemblySearchParameters.getSize(), Sort.by(assemblySearchParameters.getSortBy()));
        String productState = assemblySearchParameters.getProductStateInStore() == null ? null : assemblySearchParameters.getProductStateInStore().name();
        String deliveryMode = assemblySearchParameters.getDeliveryMode() == null ? null : assemblySearchParameters.getDeliveryMode().name();

        log.info("Search assemblies, start unix timestamp: {}, endUnixTimeStamp: {}, product state: {}, delivery mode: {}", startUnixTimestamp, endUnixTimestamp, productState, deliveryMode);
        var supplyBoxProducts = supplyBoxProductsRepository.findAllEmployeeResponse(startUnixTimestamp, endUnixTimestamp, productState, deliveryMode, keycloakId, pageRequest);


        return supplyBoxProducts.map(supplyBoxProduct -> {
            var orderAssemble = orderAssembleRepository.findByKaspiOrderId(supplyBoxProduct.getKaspiOrder().getId());
            return orderAssembleMapper.mapToEmployeeAssemblyResponse(supplyBoxProduct, orderAssemble.map(OrderAssemble::getAssembleState).orElse(null));
        });
    }

    @Override
    public AssembleProcessResponse startAssemble(JwtAuthenticationToken starterToken, String orderCode) {
        var storeEmployee = storeEmployeeRepository.findByWonderUserKeycloakId(Utils.extractIdFromToken(starterToken))
                .orElseThrow(() -> new NotAuthorizedException(""));

        var order = kaspiOrderRepository.findByCode(orderCode)
                .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.NOT_FOUND, HttpStatus.NOT_FOUND.getReasonPhrase(), "Order not found"));

        validateEmployeeWithStore(storeEmployee, order);

        var orderAssembleOptional = orderAssembleRepository.findByKaspiOrderId(order.getId());

        if (orderAssembleOptional.isPresent()) {
            throw new IllegalArgumentException("Assembly already started");
        }

        var orderAssemble = orderAssembleRepository.save(orderAssembleMapper.toEntity(storeEmployee, order, AssembleState.STARTED));

        var dividedProducts = orderAssembleMapper.divideProducts(order);

        return orderAssembleMapper.toProcessResponse(order, Utils.extractNameFromToken(starterToken), orderAssemble, dividedProducts.getLeft(), dividedProducts.getRight());
    }


    @Override
    public AssembleProductResponse assembleProduct(JwtAuthenticationToken starterToken, String productArticle, String orderCode) {
        var storeEmployee = storeEmployeeRepository.findByWonderUserKeycloakId(Utils.extractIdFromToken(starterToken))
                .orElseThrow(() -> new NotAuthorizedException(""));

        var order = kaspiOrderRepository.findByCode(orderCode)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        var store = validateEmployeeWithStore(storeEmployee, order);

        var assemble = order.getOrderAssemble();

        if (assemble == null) {
            throw new IllegalArgumentException("Assembly did not start yet");
        }

        if (assemble.getAssembleState() == AssembleState.FINISHED) {
            throw new IllegalArgumentException("Assembly already finished");
        }

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

        var dividedProducts = orderAssembleMapper.divideProducts(order);

        if (dividedProducts.getLeft().isEmpty()) {
            assemble.setAssembleState(AssembleState.READY_TO_FINISH);
        } else {
            assemble.setAssembleState(AssembleState.IN_PROGRESS);
        }

        orderAssembleRepository.save(assemble);


        var response = orderAssembleMapper.toProcessResponse(order, storeEmployee.getWonderUser().getUsername(), assemble, dividedProducts.getLeft(), dividedProducts.getRight());

        return new AssembleProductResponse(this.getWaybill(order), response);
    }


    @Override
    public AssembleProcessResponse getAssemble(JwtAuthenticationToken starterToken, String orderCode) {
        var storeEmployee = storeEmployeeRepository.findByWonderUserKeycloakId(Utils.extractIdFromToken(starterToken))
                .orElseThrow(() -> new NotAuthorizedException(""));

        var order = kaspiOrderRepository.findByCode(orderCode)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        validateEmployeeWithStore(storeEmployee, order);

        var orderAssemble = order.getOrderAssemble();

        var dividedProducts = orderAssembleMapper.divideProducts(order);


        // todo: refactor govno code
        return orderAssembleMapper.toProcessResponse(order, orderAssemble == null ? "N\\A" : orderAssemble.getStartedEmployee().getWonderUser().getUsername(), orderAssemble == null ? new OrderAssemble() : orderAssemble, dividedProducts.getLeft(), dividedProducts.getRight());

    }

    @Override
    @Transactional
    public void finishAssemble(String orderCode, String keycloakId) {
        var storeEmployee = storeEmployeeRepository.findByWonderUserKeycloakId(keycloakId)
                .orElseThrow(() -> new NotAuthorizedException(""));

        var order = kaspiOrderRepository.findByCode(orderCode)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        var assemble = getOrderAssemble(order);

        validateEmployeeWithStore(storeEmployee, order);

        var dividedProducts = orderAssembleMapper.divideProducts(order);

        if (!dividedProducts.getLeft().isEmpty()) {
            throw new IllegalArgumentException(dividedProducts.getLeft().size() + " products left to scan");
        }

        assemble.setAssembleState(AssembleState.FINISHED);
        orderAssembleRepository.save(assemble);

        order.getProducts()
                .forEach(kaspiOrderProduct -> {
                    var sbp = kaspiOrderProduct.getSupplyBoxProduct();
                    sbp.setState(ProductStateInStore.SOLD);
                    supplyBoxProductsRepository.save(sbp);
                });
    }

    private static @NotNull OrderAssemble getOrderAssemble(KaspiOrder order) {
        var assemble = order.getOrderAssemble();

        if (assemble == null) {
            throw new IllegalArgumentException("Assemble state is not ready to finish");
        }

        if (assemble.getAssembleState() == AssembleState.FINISHED) {
            throw new IllegalArgumentException("Assemble state is already finished");
        } else if (assemble.getAssembleState() != AssembleState.READY_TO_FINISH) {
            throw new IllegalArgumentException("Assemble state is not ready to finish");
        }
        return assemble;
    }

    private KaspiStore validateEmployeeWithStore(StoreEmployee storeEmployee, KaspiOrder order) {
        var storeEmployeeKaspiStore = storeEmployee.getKaspiStore();
        var orderStore = order.getKaspiStore();

        if (!storeEmployeeKaspiStore.getId().equals(orderStore.getId()))
            throw new IllegalArgumentException("Order not found");

        var orderProducts = order.getProducts();

        if (orderProducts == null || orderProducts.isEmpty())
            throw new IllegalArgumentException("Order not enabled to assemble");

        return orderStore;
    }

    private String getWaybill(KaspiOrder kaspiOrder) {
        if (kaspiOrder.getWaybill() != null) return kaspiOrder.getWaybill();
        // generate with api
        return "generated(soon)";
    }


}
