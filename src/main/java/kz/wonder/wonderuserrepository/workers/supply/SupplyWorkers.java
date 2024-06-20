package kz.wonder.wonderuserrepository.workers.supply;

import io.camunda.zeebe.spring.client.annotation.JobWorker;
import io.camunda.zeebe.spring.client.annotation.Variable;
import io.camunda.zeebe.spring.client.exception.ZeebeBpmnError;
import kz.wonder.wonderuserrepository.dto.request.SupplyCreateRequest;
import kz.wonder.wonderuserrepository.dto.response.SellerUserResponse;
import kz.wonder.wonderuserrepository.dto.response.StoreDetailResponse;
import kz.wonder.wonderuserrepository.entities.SupplyBox;
import kz.wonder.wonderuserrepository.entities.SupplyBoxProduct;
import kz.wonder.wonderuserrepository.entities.enums.ProductStateInStore;
import kz.wonder.wonderuserrepository.exceptions.DbObjectNotFoundException;
import kz.wonder.wonderuserrepository.mappers.SupplyMapper;
import kz.wonder.wonderuserrepository.repositories.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
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
    private final ProductRepository productRepository;

    @JobWorker(type = "createSupply")
    public void createSupply(@Variable SupplyCreateRequest supplyCreateRequest,
                                            @Variable StoreDetailResponse storeDto,
                                            @Variable SellerUserResponse sellerDto) {
        log.info("Create supply");


        var store = kaspiStoreRepository.findById(storeDto.getId())
                .orElseThrow(() -> new ZeebeBpmnError("400", "Store not found"));

        var wonderUser = userRepository.findByKeycloakId(sellerDto.keycloakId())
                .orElseThrow(() -> new ZeebeBpmnError("400", "User not found"));

        var supply = supplyMapper.toSupplyEntity(supplyCreateRequest, wonderUser, store);

        supplyCreateRequest.getSelectedBoxes()
                .forEach(selectedBox -> {
                    final var boxType = boxTypeRepository.findByIdInStore(selectedBox.getSelectedBoxId(), store.getId())
                            .orElseThrow(() -> new ZeebeBpmnError("400", "services-impl.supply-service-impl.supply-boxes-are-empty"));

                    var supplyBox = new SupplyBox();
                    supplyBox.setBoxType(boxType);
                    supplyBox.setSupply(supply);


                    var selectedProducts = selectedBox.getProductQuantities();
                    selectedProducts.forEach(selectedProduct -> {
                        var product = productRepository.findByIdAndKeycloakIdAndDeletedIsFalse(selectedProduct.getProductId(), wonderUser.getKeycloakId())
                                .orElseThrow(() -> new ZeebeBpmnError("400", "services-impl.supply-service-impl.product-not-found"));

                        for (int i = 0; i < selectedProduct.getQuantity(); i++) {
                            SupplyBoxProduct boxProducts = new SupplyBoxProduct();
                            boxProducts.setSupplyBox(supplyBox);
                            boxProducts.setProduct(product);
                            boxProducts.setState(ProductStateInStore.PENDING);

                            supplyBox.getSupplyBoxProducts().add(boxProducts);
                        }

                        if (supplyBox.getSupplyBoxProducts().isEmpty()) {
                            throw new ZeebeBpmnError("400", "services-impl.supply-service-impl.supply-boxes-are-empty");
                        }

                    });

                    supply.getSupplyBoxes().add(supplyBox);
                });

        supplyRepository.save(supply);
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

    @JobWorker(type = "putCityIdToParams")
    public Map<String, Object> putCityIdToParams(@Variable StoreDetailResponse storeDto) {
        log.info("Put city id to params");
        Map<String, Object> result = new HashMap<>();
        result.put("cityId", storeDto.getCity().getId());
        return result;
    }
}
