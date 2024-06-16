package kz.wonder.wonderuserrepository.dto.enums;

import kz.wonder.wonderuserrepository.entities.KaspiOrder;
import kz.wonder.wonderuserrepository.entities.OrderAssemble;
import kz.wonder.wonderuserrepository.entities.OrderPackage;
import kz.wonder.wonderuserrepository.entities.OrderTransmission;
import kz.wonder.wonderuserrepository.entities.enums.AssembleState;
import kz.wonder.wonderuserrepository.entities.enums.DeliveryMode;
import kz.wonder.wonderuserrepository.entities.enums.OrderTransmissionState;
import kz.wonder.wonderuserrepository.entities.enums.PackageState;
import lombok.Getter;

import java.util.Objects;
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
    READY_TO_SHIP_TO_COURIER_EXPRESS("Готов к отправке экспресс, ожидание курьера", OrderAvailableAction.HANDLE_TO_COURIER_EXPRESS),
    READY_TO_SHIP_TO_ZAMMLER("Готов к отправке, ожидание замлера", OrderAvailableAction.HANDLE_TO_ZAMMLER),
    READY_TO_SHIP_TO_CLIENT("Готов к отправке, ожидание клииента", OrderAvailableAction.HANDLE_TO_CLIENT),
    READY_TO_SHIP("Готов к отправке, наша доставка", OrderAvailableAction.HANDLE),
    HANDED_TO_COURIER("Передан курьеру", OrderAvailableAction.NOTHING),
    HANDED_TO_CLIENT("Передан клиенту", OrderAvailableAction.NOTHING),
    HANDED_TO_ZAMMLER("Передан замлеру", OrderAvailableAction.NOTHING),
    UNKNOWN_STATUS("Невозможно определить статус", OrderAvailableAction.NOTHING);

    private final String description;
    private final OrderAvailableAction orderAvailableAction;

    OrderStateInStore(String description, OrderAvailableAction orderAvailableAction) {
        this.description = description;
        this.orderAvailableAction = orderAvailableAction;
    }

    public static OrderStateInStore getOrderStatus(KaspiOrder kaspiOrder) {
        return getOrderStatus(getAssembleState(kaspiOrder), getPackageState(kaspiOrder), getOrderTransmissionState(kaspiOrder), kaspiOrder);
    }


    private static OrderStateInStore getOrderStatus(AssembleState assembleState, PackageState packageState, OrderTransmissionState orderTransmissionState, KaspiOrder kaspiOrder) {
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
        if (orderTransmissionState == null && (Objects.equals(kaspiOrder.getExpress(), true))) {
            return OrderStateInStore.READY_TO_SHIP_TO_COURIER_EXPRESS;
        }
        if (orderTransmissionState == null && (Objects.equals(kaspiOrder.getDeliveryMode(), DeliveryMode.DELIVERY_REGIONAL_PICKUP))) {
            return OrderStateInStore.READY_TO_SHIP_TO_ZAMMLER;
        }
        if (orderTransmissionState == null && (Objects.equals(kaspiOrder.getDeliveryMode(), DeliveryMode.DELIVERY_PICKUP))) {
            return OrderStateInStore.READY_TO_SHIP_TO_CLIENT;
        }
        if (orderTransmissionState == null && (Objects.equals(kaspiOrder.getDeliveryMode(), DeliveryMode.DELiVERY_POSTOMAT))) {
            return OrderStateInStore.READY_TO_SHIP_TO_COURIER;
        }
        if (orderTransmissionState == null && (Objects.equals(kaspiOrder.getDeliveryMode(), DeliveryMode.DELIVERY_LOCAL))) {
            return OrderStateInStore.READY_TO_SHIP;
        }
        if (orderTransmissionState == OrderTransmissionState.HANDED_TO_COURIER) {
            return OrderStateInStore.HANDED_TO_COURIER;
        }
        if (orderTransmissionState == OrderTransmissionState.HANDED_TO_CLIENT) {
            return OrderStateInStore.HANDED_TO_CLIENT;
        }
        if (orderTransmissionState == OrderTransmissionState.HANDED_TO_ZAMLER) {
            return OrderStateInStore.HANDED_TO_ZAMMLER;
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