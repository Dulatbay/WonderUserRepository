package kz.wonder.wonderuserrepository.mappers;

import kz.wonder.wonderuserrepository.constants.Utils;
import kz.wonder.wonderuserrepository.dto.response.AssembleProcessResponse;
import kz.wonder.wonderuserrepository.dto.response.EmployeeAssemblyResponse;
import kz.wonder.wonderuserrepository.entities.*;
import kz.wonder.wonderuserrepository.exceptions.DbObjectNotFoundException;
import kz.wonder.wonderuserrepository.repositories.StoreCellProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;

@Slf4j
@Component
public class OrderAssembleMapper {
    private final StoreCellProductRepository storeCellProductRepository;

    public OrderAssembleMapper(StoreCellProductRepository storeCellProductRepository) {
        this.storeCellProductRepository = storeCellProductRepository;
    }

    public OrderAssemble toEntity(StoreEmployee storeEmployee, KaspiOrder kaspiOrder, AssembleState assembleState) {
        OrderAssemble orderAssemble = new OrderAssemble();
        orderAssemble.setAssembleState(AssembleState.STARTED);
        orderAssemble.setStartedEmployee(storeEmployee);
        orderAssemble.setKaspiOrder(kaspiOrder);
        return orderAssemble;
    }

    public AssembleProcessResponse toProcessResponse(KaspiOrder kaspiOrder, String starterName, Long assembleId) {
        AssembleProcessResponse assembleProcessResponse = new AssembleProcessResponse();
        assembleProcessResponse.setSellerName(kaspiOrder.getKaspiStore().getWonderUser().getKaspiToken().getSellerName());
        assembleProcessResponse.setDeadline(Utils.getLocalDateTimeFromTimestamp(kaspiOrder.getCourierTransmissionPlanningDate()));
        assembleProcessResponse.setProcessedProducts(new ArrayList<>());
        assembleProcessResponse.setProductsToProcess(new ArrayList<>());
        assembleProcessResponse.setDeliveryMode(kaspiOrder.getDeliveryMode());
        assembleProcessResponse.setStartedEmployeeName(starterName);
        assembleProcessResponse.setAssembleId(assembleId);
        assembleProcessResponse.setOrderCode(kaspiOrder.getCode());

        kaspiOrder.getProducts()
                .forEach(kaspiOrderProduct -> {

                    var supplyBoxProduct = kaspiOrderProduct.getSupplyBoxProduct();
                    var product = kaspiOrderProduct.getProduct();
                    var storeCellProduct = storeCellProductRepository.findBySupplyBoxProductId(supplyBoxProduct.getId())
                            .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase(), "Something get wrong"));

                    if (supplyBoxProduct.getState() == ProductStateInStore.WAITING_FOR_ASSEMBLY) {
                        var productResponse = new AssembleProcessResponse.Product();
                        productResponse.setId(product.getId());
                        productResponse.setName(product.getName());
                        productResponse.setArticle(supplyBoxProduct.getArticle());
                        productResponse.setCellCode(storeCellProduct.getStoreCell().getCode());
                        assembleProcessResponse.getProductsToProcess().add(productResponse);
                    } else {
                        var processedProduct = new AssembleProcessResponse.ProcessedProduct();
                        var assembleProcess = storeCellProduct.getAssembleProcess();

                        processedProduct.setId(product.getId());
                        processedProduct.setName(product.getName());
                        processedProduct.setArticle(supplyBoxProduct.getArticle());
                        processedProduct.setCellCode(storeCellProduct.getStoreCell().getCode());
                        processedProduct.setProcessedDate(LocalDateTime.now());
                        processedProduct.setProcessedEmployeeName(assembleProcess != null ? assembleProcess.getStoreEmployee().getWonderUser().getUsername() : "N\\A");
                        processedProduct.setWaybill(kaspiOrder.getWaybill());
                        assembleProcessResponse.getProcessedProducts().add(processedProduct);
                    }
                });

        return assembleProcessResponse;
    }

    public OrderAssembleProcess toOrderAssembleProcessEntity(KaspiOrder kaspiOrder, StoreEmployee storeEmployee, StoreCellProduct storeCellProduct) {
        OrderAssembleProcess orderAssembleProcess = new OrderAssembleProcess();
        orderAssembleProcess.setOrderAssemble(kaspiOrder.getOrderAssemble());
        orderAssembleProcess.setStoreEmployee(storeEmployee);
        orderAssembleProcess.setStoreCellProduct(storeCellProduct);
        return orderAssembleProcess;
    }

    public EmployeeAssemblyResponse mapToEmployeeAssemblyResponse(SupplyBoxProduct supplyBoxProduct) {
        var sellerName = supplyBoxProduct.getSupplyBox().getSupply().getAuthor().getKaspiToken().getSellerName();
        var order = supplyBoxProduct.getKaspiOrder();
        EmployeeAssemblyResponse response = new EmployeeAssemblyResponse();
        response.setShopName(sellerName);
        response.setOrderCode(order == null ? "N\\A" : order.getCode());
        response.setDeliveryMode(order == null ? null : order.getDeliveryMode());
        response.setOrderId(order == null ? null : order.getId());
        response.setOrderDate(Utils.getLocalDateTimeFromTimestamp(order != null ? order.getCreationDate() : 0));
        response.setDeliveryDate(Utils.getLocalDateTimeFromTimestamp(order != null ? order.getPlannedDeliveryDate() : 0));
        response.setProductsCount(order != null ? order.getProducts().size() : 0);
        return response;
    }

}