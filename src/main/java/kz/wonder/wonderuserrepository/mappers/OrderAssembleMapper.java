package kz.wonder.wonderuserrepository.mappers;

import kz.wonder.wonderuserrepository.constants.Utils;
import kz.wonder.wonderuserrepository.dto.response.AssembleProcessResponse;
import kz.wonder.wonderuserrepository.entities.*;
import kz.wonder.wonderuserrepository.exceptions.DbObjectNotFoundException;
import kz.wonder.wonderuserrepository.repositories.StoreCellProductRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

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
        orderAssemble.setOrderId(kaspiOrder.getId());
        return orderAssemble;
    }

    public AssembleProcessResponse toProcessResponse(KaspiOrder kaspiOrder, String starterName) {
        AssembleProcessResponse assembleProcessResponse = new AssembleProcessResponse();
        assembleProcessResponse.setSellerName(kaspiOrder.getKaspiStore().getWonderUser().getKaspiToken().getSellerName());
        assembleProcessResponse.setDeadline(Utils.getLocalDateTimeFromTimestamp(kaspiOrder.getCourierTransmissionPlanningDate()));
        assembleProcessResponse.setProcessedProducts(new ArrayList<>());
        assembleProcessResponse.setProductsToProcess(new ArrayList<>());
        assembleProcessResponse.setDeliveryMode(kaspiOrder.getDeliveryMode());
        assembleProcessResponse.setStartedEmployeeName(starterName);


        kaspiOrder.getProducts()
                .forEach(kaspiOrderProduct -> {
                    var productResponse = new AssembleProcessResponse.Product();

                    var supplyBoxProduct = kaspiOrderProduct.getSupplyBoxProduct();
                    var product = kaspiOrderProduct.getProduct();
                    var storeCellProduct = storeCellProductRepository.findBySupplyBoxProductId(supplyBoxProduct.getId())
                            .orElseThrow(() -> new DbObjectNotFoundException(HttpStatus.BAD_REQUEST, HttpStatus.BAD_REQUEST.getReasonPhrase(), "Something get wrong"));

                    productResponse.setId(product.getId());
                    productResponse.setName(product.getName());
                    productResponse.setArticle(supplyBoxProduct.getArticle());
                    productResponse.setCellCode(storeCellProduct.getStoreCell().getCode());

                    if (supplyBoxProduct.getState() == ProductStateInStore.WAITING_FOR_ASSEMBLY)
                        assembleProcessResponse.getProductsToProcess().add(productResponse);
                    else
                        assembleProcessResponse.getProcessedProducts().add(productResponse);

                });

        return assembleProcessResponse;
    }
}
