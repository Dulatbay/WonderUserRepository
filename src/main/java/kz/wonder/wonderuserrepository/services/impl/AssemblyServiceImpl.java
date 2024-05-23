package kz.wonder.wonderuserrepository.services.impl;

import kz.wonder.wonderuserrepository.constants.Utils;
import kz.wonder.wonderuserrepository.dto.params.AssemblySearchParameters;
import kz.wonder.wonderuserrepository.dto.response.AssembleProcessResponse;
import kz.wonder.wonderuserrepository.dto.response.EmployeeAssemblyResponse;
import kz.wonder.wonderuserrepository.entities.AssembleState;
import kz.wonder.wonderuserrepository.entities.SupplyBoxProduct;
import kz.wonder.wonderuserrepository.mappers.OrderAssembleMapper;
import kz.wonder.wonderuserrepository.repositories.KaspiOrderRepository;
import kz.wonder.wonderuserrepository.repositories.OrderAssembleRepository;
import kz.wonder.wonderuserrepository.repositories.StoreEmployeeRepository;
import kz.wonder.wonderuserrepository.repositories.SupplyBoxProductsRepository;
import kz.wonder.wonderuserrepository.services.AssemblyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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


    @Override
    public Page<EmployeeAssemblyResponse> findAssembliesByParams(String keycloakId, AssemblySearchParameters assemblySearchParameters) {
        long startUnixTimestamp = assemblySearchParameters.getOrderCreationStartDate().atStartOfDay().atZone(ZONE_ID).toInstant().getEpochSecond() * 1000;
        long endUnixTimestamp = assemblySearchParameters.getOrderCreationEndDate().atStartOfDay().plusDays(1).atZone(ZONE_ID).toInstant().getEpochSecond() * 1000;

        PageRequest pageRequest = PageRequest.of(assemblySearchParameters.getPage(), assemblySearchParameters.getSize(), Sort.by(assemblySearchParameters.getSortBy()));
        String productState = assemblySearchParameters.getProductStateInStore() == null ? null : assemblySearchParameters.getProductStateInStore().name();
        String deliveryMode = assemblySearchParameters.getDeliveryMode() == null ? null : assemblySearchParameters.getDeliveryMode().name();

        log.info("start unix timestamp: {}, endUnixTimeStamp: {}, product state: {}, delivery mode: {}", startUnixTimestamp, endUnixTimestamp, productState, deliveryMode);
        var products = supplyBoxProductsRepository.findAllEmployeeResponse(startUnixTimestamp, endUnixTimestamp, productState, deliveryMode, pageRequest);

        return products.map(this::mapToEmployeeAssemblyResponse);
    }

    @Override
    public AssembleProcessResponse startAssemble(JwtAuthenticationToken starterToken, String orderCode) {
        var storeEmployee = storeEmployeeRepository.findByWonderUserKeycloakId(Utils.extractIdFromToken(starterToken))
                .orElseThrow(() -> new NotAuthorizedException(""));

        var order = kaspiOrderRepository.findByCode(orderCode)
                .orElseThrow(() -> new IllegalStateException("Order not found"));

        var storeEmployeeKaspiStore = storeEmployee.getKaspiStore();
        var orderStore = order.getKaspiStore();

        if (!storeEmployeeKaspiStore.getId().equals(orderStore.getId()))
            throw new IllegalArgumentException("Order not found");

        var orderProducts = order.getProducts();

        if (orderProducts == null || orderProducts.isEmpty())
            throw new IllegalArgumentException("Order not enabled to assemble");


        orderAssembleRepository.save(orderAssembleMapper.toEntity(storeEmployee, order, AssembleState.STARTED));

        return orderAssembleMapper.toProcessResponse(order, Utils.extractNameFromToken(starterToken));
    }


    private EmployeeAssemblyResponse mapToEmployeeAssemblyResponse(SupplyBoxProduct supplyBoxProduct) {
        EmployeeAssemblyResponse response = new EmployeeAssemblyResponse();
        response.setShopName(supplyBoxProduct.getArticle());
        // todo:
        return response;
    }
}
