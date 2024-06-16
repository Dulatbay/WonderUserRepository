package kz.wonder.wonderuserrepository.dto.enums;

import lombok.Getter;

@Getter
public enum OrderTransmissionPerson {
    CLIENT("Клиент"),
    EXPRESS_COURIER("Курьерская служба"),
    KASPI_COURIER("Каспи курьер"),
    STORE_COURIER("Силами продавца"),
    ZAMLER("Заммлер"),
    UNKNOWN("Еще неизвестно");

    private final String description;

    OrderTransmissionPerson(String description) {
        this.description = description;
    }

    public static OrderTransmissionPerson getByOrderStateInStore(OrderStateInStore orderStateInStore) {
        return switch (orderStateInStore) {
            case READY_TO_SHIP -> OrderTransmissionPerson.STORE_COURIER;
            case READY_TO_SHIP_TO_COURIER_EXPRESS -> OrderTransmissionPerson.EXPRESS_COURIER;
            case READY_TO_SHIP_TO_ZAMMLER -> OrderTransmissionPerson.ZAMLER;
            case READY_TO_SHIP_TO_CLIENT -> OrderTransmissionPerson.CLIENT;
            default -> OrderTransmissionPerson.UNKNOWN;
        };
    }
}
