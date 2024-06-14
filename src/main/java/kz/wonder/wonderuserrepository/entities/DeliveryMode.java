package kz.wonder.wonderuserrepository.entities;

public enum DeliveryMode {
    DELIVERY_LOCAL,
    DELIVERY_PICKUP,
    DELIVERY_REGIONAL_PICKUP,
    DELiVERY_POSTOMAT,
    DELIVERY_REGIONAL_TODOOR;

    public static DeliveryMode buildDeliveryMode(String deliveryModeStr, boolean isKaspiDelivery) {
        var deliveryMode = DeliveryMode.valueOf(deliveryModeStr);

        if (isKaspiDelivery && deliveryMode == DeliveryMode.DELIVERY_PICKUP)
            deliveryMode = DeliveryMode.DELIVERY_REGIONAL_PICKUP;

        return deliveryMode;
    }
}
