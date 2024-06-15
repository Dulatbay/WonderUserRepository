package kz.wonder.wonderuserrepository.entities;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum DeliveryMode {
    DELIVERY_LOCAL("Наша отправка"),
    DELIVERY_PICKUP("Самовывоз"),
    DELIVERY_REGIONAL_PICKUP("Самовывоз cо склада каспи"),
    DELiVERY_POSTOMAT("Kaspi Postomat"),
    DELIVERY_REGIONAL_TODOOR("Kaspi Доставка");

    @JsonValue
    private final String description;

    DeliveryMode(String description) {
        this.description = description;
    }

    public static DeliveryMode buildDeliveryMode(String deliveryModeStr, boolean isKaspiDelivery) {
        var deliveryMode = DeliveryMode.valueOf(deliveryModeStr);

        if (isKaspiDelivery && deliveryMode == DeliveryMode.DELIVERY_PICKUP)
            deliveryMode = DeliveryMode.DELIVERY_REGIONAL_PICKUP;

        return deliveryMode;
    }

    @Override
    public String toString() {
        return this.description;
    }
}
