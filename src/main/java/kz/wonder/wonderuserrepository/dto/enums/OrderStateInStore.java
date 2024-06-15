package kz.wonder.wonderuserrepository.dto.enums;

import kz.wonder.wonderuserrepository.entities.*;
import kz.wonder.wonderuserrepository.entities.enums.AssembleState;
import kz.wonder.wonderuserrepository.entities.enums.OrderTransmissionState;
import kz.wonder.wonderuserrepository.entities.enums.PackageState;
import lombok.Getter;

import java.util.Optional;

@Getter
public enum OrderStateInStore {
    ASSEMBLY_NOT_STARTED("Сборка не начата", OrderAvailableAction.START_ASSEMBLE),
    ASSEMBLY_IN_PROGRESS("Идет сборка", OrderAvailableAction.ASSEMBLE_PRODUCT),
    READY_TO_FINISH_ASSEMBLY("Готова к завершению сборки", OrderAvailableAction.FINISH_ASSEMBLE),
    PACKAGING_NOT_STARTED("Упаковка не начата", OrderAvailableAction.START_PACKING),
    PACKAGING_IN_PROGRESS("Идет упаковка", OrderAvailableAction.PACK_PRODUCT),
    READY_TO_FINISH_PACKAGING("Готов к завершению упаковки", OrderAvailableAction.FINISH_PACKING),
    READY_TO_SHIP_TO_COURIER("Готов к отправке, ожидание курьера", OrderAvailableAction.HANDLE_TO_COURIER),
    READY_TO_SHIP_TO_CLIENT("Готов к отправке, ожидание клииента", OrderAvailableAction.HANDLE_TO_CLIENT),
    HANDED_TO_COURIER("Передан курьеру", OrderAvailableAction.NOTHING),
    HANDED_TO_CLIENT("Передан клиенту", OrderAvailableAction.NOTHING),
    UNKNOWN_STATUS("Невозможно определить статус", OrderAvailableAction.NOTHING);

    private final String description;
    private final OrderAvailableAction orderAvailableAction;

    OrderStateInStore(String description, OrderAvailableAction orderAvailableAction) {
        this.description = description;
        this.orderAvailableAction = orderAvailableAction;
    }

    public static OrderStateInStore getOrderStatus(KaspiOrder kaspiOrder) {
        return getOrderStatus(getAssembleState(kaspiOrder), getPackageState(kaspiOrder), getOrderTransmissionState(kaspiOrder));
    }

    private static OrderStateInStore getOrderStatus(AssembleState assembleState, PackageState packageState, OrderTransmissionState orderTransmissionState) {
        if (assembleState == null) {
            return OrderStateInStore.ASSEMBLY_NOT_STARTED;
        }
        if (assembleState == AssembleState.IN_PROGRESS || assembleState == AssembleState.STARTED) {
            return OrderStateInStore.ASSEMBLY_IN_PROGRESS;
        }
        if (assembleState == AssembleState.READY_TO_FINISH) {
            return OrderStateInStore.READY_TO_FINISH_ASSEMBLY;
        }
        if (packageState == null) {
            return OrderStateInStore.PACKAGING_NOT_STARTED;
        }
        if (packageState == PackageState.STARTED || packageState == PackageState.IN_PROGRESS) {
            return OrderStateInStore.PACKAGING_IN_PROGRESS;
        }
        if (packageState == PackageState.READY_TO_FINISH) {
            return OrderStateInStore.READY_TO_FINISH_PACKAGING;
        }
        if (orderTransmissionState == null) {
            return OrderStateInStore.READY_TO_SHIP_TO_COURIER;
        }
        if (orderTransmissionState == OrderTransmissionState.HANDED_TO_COURIER) {
            return OrderStateInStore.HANDED_TO_COURIER;
        }
        if (orderTransmissionState == OrderTransmissionState.HANDED_TO_CLIENT) {
            return OrderStateInStore.HANDED_TO_CLIENT;
        }
        return OrderStateInStore.UNKNOWN_STATUS;
    }

    private static AssembleState getAssembleState(KaspiOrder kaspiOrder) {
        return Optional.ofNullable(kaspiOrder.getOrderAssemble())
                .map(OrderAssemble::getAssembleState)
                .orElse(null);
    }

    private static PackageState getPackageState(KaspiOrder kaspiOrder) {
        return Optional.ofNullable(kaspiOrder.getOrderPackage())
                .map(OrderPackage::getPackageState)
                .orElse(null);
    }

    private static OrderTransmissionState getOrderTransmissionState(KaspiOrder kaspiOrder) {
        return Optional.ofNullable(kaspiOrder.getOrderTransmission())
                .map(OrderTransmission::getOrderTransmissionState)
                .orElse(null);
    }


}