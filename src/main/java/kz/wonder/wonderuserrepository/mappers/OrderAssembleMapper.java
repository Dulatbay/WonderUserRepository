package kz.wonder.wonderuserrepository.mappers;

import kz.wonder.wonderuserrepository.constants.Utils;
import kz.wonder.wonderuserrepository.dto.response.AssembleProcessResponse;
import kz.wonder.wonderuserrepository.dto.response.EmployeeAssemblyResponse;
import kz.wonder.wonderuserrepository.entities.*;
import kz.wonder.wonderuserrepository.entities.enums.AssembleState;
import kz.wonder.wonderuserrepository.entities.enums.ProductStateInStore;
import kz.wonder.wonderuserrepository.exceptions.DbObjectNotFoundException;
import kz.wonder.wonderuserrepository.repositories.StoreCellProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderAssembleMapper {
    private final StoreCellProductRepository storeCellProductRepository;
    private final MessageSource messageSource;

    public OrderAssemble toEntity(StoreEmployee storeEmployee, KaspiOrder kaspiOrder, AssembleState assembleState) {
        OrderAssemble orderAssemble = new OrderAssemble();
        orderAssemble.setAssembleState(assembleState);
        orderAssemble.setStartedEmployee(storeEmployee);
        orderAssemble.setKaspiOrder(kaspiOrder);
        return orderAssemble;
    }

    public AssembleProcessResponse toProcessResponse(KaspiOrder kaspiOrder, String starterName, OrderAssemble orderAssemble, List<AssembleProcessResponse.Product> productsToProcess, List<AssembleProcessResponse.ProcessedProduct> processedProducts) {
        AssembleProcessResponse assembleProcessResponse = new AssembleProcessResponse();
        assembleProcessResponse.setSellerName(kaspiOrder.getWonderUser().getKaspiToken().getSellerName());
        assembleProcessResponse.setDeadline(Utils.getLocalDateTimeFromTimestamp(kaspiOrder.getCourierTransmissionPlanningDate()));
        assembleProcessResponse.setDeliveryMode(kaspiOrder.getDeliveryMode());
        assembleProcessResponse.setStartedEmployeeName(starterName);
        assembleProcessResponse.setOrderCode(kaspiOrder.getCode());
        assembleProcessResponse.setAssembleState(orderAssemble.getAssembleState() == null ? AssembleState.WAITING_TO_ASSEMBLE : orderAssemble.getAssembleState());


        assembleProcessResponse.setProductsToProcess(productsToProcess);
        assembleProcessResponse.setProcessedProducts(processedProducts);

        return assembleProcessResponse;
    }

    public Pair<List<AssembleProcessResponse.Product>, List<AssembleProcessResponse.ProcessedProduct>> divideProducts(KaspiOrder kaspiOrder) {
        List<AssembleProcessResponse.Product> productsToProcess = new ArrayList<>();
        List<AssembleProcessResponse.ProcessedProduct> processedProducts = new ArrayList<>();

        if (kaspiOrder.getProducts() == null) {
            return Pair.of(productsToProcess, processedProducts);
        }

        kaspiOrder.getProducts()
                .forEach(kaspiOrderProduct -> {

                    var supplyBoxProduct = kaspiOrderProduct.getSupplyBoxProduct();
                    var product = kaspiOrderProduct.getProduct();
                    var storeCellProduct = storeCellProductRepository.findBySupplyBoxProductId(supplyBoxProduct.getId())
                            .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase(), messageSource.getMessage(
                                    "mappers.order-assemble-mapper.something-get-wrong",
                                    null,
                                    LocaleContextHolder.getLocale()
                            )));

                    if (supplyBoxProduct.getState() == ProductStateInStore.WAITING_FOR_ASSEMBLY) {
                        var productResponse = new AssembleProcessResponse.Product();
                        productResponse.setId(product.getId());
                        productResponse.setName(product.getName());
                        productResponse.setArticle(supplyBoxProduct.getArticle());
                        productResponse.setCellCode(storeCellProduct.getStoreCell().getCode());
                        productsToProcess.add(productResponse);
                    } else {
                        var processedProduct = new AssembleProcessResponse.ProcessedProduct();
                        var assembleProcess = storeCellProduct.getAssembleProcess();

                        processedProduct.setId(product.getId());
                        processedProduct.setName(product.getName());
                        processedProduct.setArticle(supplyBoxProduct.getArticle());
                        processedProduct.setCellCode(storeCellProduct.getStoreCell().getCode());
                        processedProduct.setProcessedDate(LocalDateTime.now());
                        processedProduct.setProcessedEmployeeName(assembleProcess != null ? assembleProcess.getStoreEmployee().getWonderUser().getUsername() : "N\\A");
                        processedProducts.add(processedProduct);
                    }
                });

        return Pair.of(productsToProcess, processedProducts);
    }

    public OrderAssembleProcess toOrderAssembleProcessEntity(KaspiOrder kaspiOrder, StoreEmployee storeEmployee, StoreCellProduct storeCellProduct) {
        OrderAssembleProcess orderAssembleProcess = new OrderAssembleProcess();
        orderAssembleProcess.setOrderAssemble(kaspiOrder.getOrderAssemble());
        orderAssembleProcess.setStoreEmployee(storeEmployee);
        orderAssembleProcess.setStoreCellProduct(storeCellProduct);
        return orderAssembleProcess;
    }

    public static final String NO_ORDER_CODE = "N\\A";

    public EmployeeAssemblyResponse mapToEmployeeAssemblyResponse(SupplyBoxProduct supplyBoxProduct, AssembleState assembleState) {
        if (supplyBoxProduct == null || assembleState == null)
            return new EmployeeAssemblyResponse();

        var sellerName = supplyBoxProduct.getSupplyBox().getSupply().getAuthor().getKaspiToken().getSellerName();
        var order = supplyBoxProduct.getKaspiOrder();

        EmployeeAssemblyResponse response = new EmployeeAssemblyResponse();
        response.setShopName(sellerName);
        response.setAssembleState(assembleState);
        setResponseWithOrderData(response, order);
        return response;
    }

    private void setResponseWithOrderData(EmployeeAssemblyResponse response, KaspiOrder order) {
        if (order == null) {
            response.setOrderCode(NO_ORDER_CODE);
            response.setOrderId(null);
            response.setOrderDate(Utils.getLocalDateTimeFromTimestamp(0L));
            response.setProductsCount(0);
        } else {
            response.setOrderCode(order.getCode());
            response.setDeliveryMode(order.getDeliveryMode());
            response.setOrderId(order.getId());
            response.setOrderDate(Utils.getLocalDateTimeFromTimestamp(order.getCreationDate()));
            if (order.getPlannedDeliveryDate() != null) {
                response.setDeliveryDate(Utils.getLocalDateTimeFromTimestamp(order.getPlannedDeliveryDate()));
            }
            response.setProductsCount(Optional.ofNullable(order.getProducts()).map(Set::size).orElse(0));
        }
    }

}
