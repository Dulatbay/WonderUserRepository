package kz.wonder.wonderuserrepository.workers.store;

import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import io.camunda.zeebe.spring.client.annotation.Variable;
import io.camunda.zeebe.spring.client.exception.ZeebeBpmnError;
import kz.wonder.wonderuserrepository.mappers.KaspiStoreMapper;
import kz.wonder.wonderuserrepository.repositories.BoxTypeRepository;
import kz.wonder.wonderuserrepository.repositories.KaspiStoreAvailableBoxTypesRepository;
import kz.wonder.wonderuserrepository.repositories.KaspiStoreAvailableTimesRepository;
import kz.wonder.wonderuserrepository.repositories.KaspiStoreRepository;
import kz.wonder.wonderuserrepository.workers.store.dto.StoreAddress;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class StoreWorkers {
    private final KaspiStoreRepository kaspiStoreRepository;
    private final KaspiStoreMapper kaspiStoreMapper;
    private final BoxTypeRepository boxTypeRepository;
    private final KaspiStoreAvailableBoxTypesRepository kaspiStoreAvailableBoxTypesRepository;
    private final KaspiStoreAvailableTimesRepository kaspiStoreAvailableTimesRepository;

    @JobWorker(type = "findStoreByAddress")
    public Map<String, Object> findStoreByAddress(@Variable StoreAddress storeAddress) {
        log.info("Find store by address {}", storeAddress);
        Map<String, Object> result = new HashMap<>();

        kaspiStoreRepository.findByStoreAddress(storeAddress.streetName(), storeAddress.streetNumber(), storeAddress.cityId())
                .ifPresent(storeEntity -> result.put("storeDto", kaspiStoreMapper.mapToDetailResponse(storeEntity)));


        return result;
    }

    @JobWorker(type = "getStoreById", timeout = 1000)
    public Map<String, Object> getStoreById(final ActivatedJob job) {
        Map<String, Object> variables = job.getVariablesAsMap();

        var storeId = Long.parseLong(variables.get("storeId").toString());

        log.info("Get store by id {}", storeId);

        var kaspiStore = kaspiStoreRepository.findById(storeId)
                .orElseThrow(() -> new ZeebeBpmnError("400", "store-does-not-exist"));

        var availableBoxTypes = kaspiStoreAvailableBoxTypesRepository.findByKaspiStoreId(storeId);

        var availableTimes = kaspiStoreAvailableTimesRepository.findByKaspiStoreId(storeId);

        kaspiStore.setAvailableBoxTypes(new HashSet<>(availableBoxTypes));
        kaspiStore.setAvailableTimes(new HashSet<>(availableTimes));

        log.info("mapping to response");

        variables.put("storeDto", kaspiStoreMapper.mapToDetailResponse(kaspiStore));

        log.info("mapped to response");


        return variables;
    }

    @JobWorker(type = "PrintHelloWorld")
    public void printHelloWorld() {
        log.info("Hello world");
    }
}
