package kz.wonder.wonderuserrepository.workers.store;

import io.camunda.zeebe.spring.client.annotation.JobWorker;
import io.camunda.zeebe.spring.client.annotation.Variable;
import io.camunda.zeebe.spring.client.annotation.VariablesAsType;
import io.camunda.zeebe.spring.client.exception.ZeebeBpmnError;
import kz.wonder.wonderuserrepository.mappers.KaspiStoreMapper;
import kz.wonder.wonderuserrepository.repositories.KaspiStoreRepository;
import kz.wonder.wonderuserrepository.workers.store.dto.StoreAddress;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class StoreWorkers {
    private final KaspiStoreRepository kaspiStoreRepository;
    private final KaspiStoreMapper kaspiStoreMapper;

    @JobWorker(type = "findStoreByAddress")
    public Map<String, Object> findStoreByAddress(@VariablesAsType StoreAddress storeAddress) {
        log.info("Find store by address {}", storeAddress);
        Map<String, Object> result = new HashMap<>();

        kaspiStoreRepository.findByStoreAddress(storeAddress.streetName(), storeAddress.streetNumber(), storeAddress.cityId())
                .ifPresent(storeEntity -> result.put("storeDto", kaspiStoreMapper.mapToDetailResponse(storeEntity)));


        return result;
    }

    @JobWorker(type = "getStoreById")
    public Map<String, Object> getStoreById(@Variable Long storeId) {
        log.info("Get store by id {}", storeId);
        Map<String, Object> result = new HashMap<>();

        var kaspiStore = kaspiStoreRepository.findById(storeId)
                .orElseThrow(() -> new ZeebeBpmnError("400", "store-disabled"));

        result.put("storeDto", kaspiStoreMapper.mapToDetailResponse(kaspiStore));

        return result;
    }

    @JobWorker(type = "PrintHelloWorld")
    public void printHelloWorld() {
        log.info("Hello world");
    }
}
