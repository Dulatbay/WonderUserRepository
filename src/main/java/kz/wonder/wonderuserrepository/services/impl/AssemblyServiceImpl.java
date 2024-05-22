package kz.wonder.wonderuserrepository.services.impl;

import kz.wonder.wonderuserrepository.dto.params.AssemblySearchParameters;
import kz.wonder.wonderuserrepository.dto.response.EmployeeAssemblyResponse;
import kz.wonder.wonderuserrepository.entities.SupplyBoxProduct;
import kz.wonder.wonderuserrepository.repositories.SupplyBoxProductsRepository;
import kz.wonder.wonderuserrepository.services.AssemblyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import static kz.wonder.wonderuserrepository.constants.ValueConstants.ZONE_ID;

@Service
@Slf4j
@RequiredArgsConstructor
public class AssemblyServiceImpl implements AssemblyService {
    private final SupplyBoxProductsRepository supplyBoxProductsRepository;


    @Override
    public Page<EmployeeAssemblyResponse> findAssembliesByParams(String keycloakId, AssemblySearchParameters assemblySearchParameters) {
        PageRequest pageRequest = PageRequest.of(assemblySearchParameters.getPage(), assemblySearchParameters.getSize(), Sort.by(assemblySearchParameters.getSortBy()));
        long startUnixTimestamp = assemblySearchParameters.getStartDate().atStartOfDay().atZone(ZONE_ID).toInstant().getEpochSecond();
        long endUnixTimestamp = assemblySearchParameters.getEndDate().atStartOfDay().plusDays(1).atZone(ZONE_ID).toInstant().getEpochSecond();

        log.info("start unix timestamp: {}, endUnixTimeStamp: {}", startUnixTimestamp, endUnixTimestamp);

        var products = supplyBoxProductsRepository.findAllEmployeeResponse(pageRequest);
        log.info("products size {}", products.getSize());

        return products.map(this::mapToEmployeeAssemblyResponse);
    }


    private EmployeeAssemblyResponse mapToEmployeeAssemblyResponse(SupplyBoxProduct supplyBoxProduct) {
        EmployeeAssemblyResponse response = new EmployeeAssemblyResponse();
        response.setShopName(supplyBoxProduct.getArticle());
        return response;
    }
}
