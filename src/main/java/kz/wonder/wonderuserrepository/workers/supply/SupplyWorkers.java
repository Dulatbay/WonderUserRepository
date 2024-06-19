package kz.wonder.wonderuserrepository.workers.supply;

import io.camunda.zeebe.spring.client.annotation.JobWorker;
import io.camunda.zeebe.spring.client.annotation.Variable;
import io.camunda.zeebe.spring.client.exception.ZeebeBpmnError;
import kz.wonder.wonderuserrepository.dto.request.SupplyCreateRequest;
import kz.wonder.wonderuserrepository.dto.response.SellerUserResponse;
import kz.wonder.wonderuserrepository.dto.response.StoreDetailResponse;
import kz.wonder.wonderuserrepository.mappers.SupplyMapper;
import kz.wonder.wonderuserrepository.repositories.BoxTypeRepository;
import kz.wonder.wonderuserrepository.repositories.KaspiStoreRepository;
import kz.wonder.wonderuserrepository.repositories.SupplyRepository;
import kz.wonder.wonderuserrepository.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class SupplyWorkers {


    private final SupplyMapper supplyMapper;
    private final KaspiStoreRepository kaspiStoreRepository;
    private final SupplyRepository supplyRepository;
    private final BoxTypeRepository boxTypeRepository;
    private final UserRepository userRepository;

    @JobWorker(type = "createSupply")
    public Map<String, Object> createSupply(@Variable SupplyCreateRequest supplyCreateRequest,
                                            @Variable StoreDetailResponse storeDto,
                                            @Variable SellerUserResponse sellerDto) {
        log.info("Create supply");

        Map<String, Object> result = new HashMap<>();

        var kaspiStore = kaspiStoreRepository.findById(storeDto.getId())
                .orElseThrow(() -> new ZeebeBpmnError("400", "Store not found"));

        var wonderUser = userRepository.findByKeycloakId(sellerDto.keycloakId())
                .orElseThrow(() -> new ZeebeBpmnError("400", "User not found"));

        var supplyEntity = supplyMapper.toSupplyEntity(supplyCreateRequest, wonderUser, kaspiStore);

        supplyRepository.save(supplyEntity);

        return result;
    }

    @JobWorker(type = "checkStoreAndSupplyTime")
    public Map<String, Object> checkStoreAndSupplyTime(@Variable SupplyCreateRequest supplyCreateRequest, @Variable StoreDetailResponse storeDto) {
        log.info("Check store and supply time");
        Map<String, Object> result = new HashMap<>();

        var availableTimes = storeDto.getAvailableWorkTimes();
        var selectedTime = supplyCreateRequest.getSelectedTime();
        var dayOfWeekOfSelectedTime = selectedTime.getDayOfWeek();


        var isAvailableToSupply = false;


        for (var time : availableTimes) {
            if (time.getDayOfWeek() == dayOfWeekOfSelectedTime.ordinal()) {
//                if (time.getCloseTime().isAfter(selectedTime.toLocalTime().minusMinutes(1)) && time.getOpenTime().isBefore(selectedTime.toLocalTime().plusMinutes(1))) {
                isAvailableToSupply = true;
                break;
//                }
            }
        }

        result.put("isAvailableToCreateSupply", isAvailableToSupply);

        return result;
    }

    @JobWorker(type = "checkStoreAndSupplyBox")
    public Map<String, Object> checkStoreAndSupplyBox(@Variable SupplyCreateRequest supplyCreateRequest, @Variable StoreDetailResponse storeDto) {
        log.info("Check store and supply box");
        Map<String, Object> result = new HashMap<>();

        var boxTypeIds = supplyCreateRequest.getSelectedBoxes().stream().map(SupplyCreateRequest.SelectedBox::getSelectedBoxId).toList();

        var foundBoxes = boxTypeRepository.findByIdsInStore(boxTypeIds, storeDto.getId());

        var isAvailableToSupply = (boxTypeIds.size() == foundBoxes.size());

        result.put("isAvailableToCreateSupply", isAvailableToSupply);

        return result;
    }
}
