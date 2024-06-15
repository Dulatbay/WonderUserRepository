package kz.wonder.wonderuserrepository.dto.response;

import kz.wonder.wonderuserrepository.dto.enums.OrderAvailableAction;
import kz.wonder.wonderuserrepository.dto.enums.OrderStateInStore;
import kz.wonder.wonderuserrepository.entities.enums.DeliveryMode;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EmployeeOrderResponse {
    private String orderCode;
    private String shopName;
    private String formattedAddress;
    private LocalDateTime orderCreatedAt;
    private LocalDateTime orderToSendTime;
    private DeliveryMode deliveryType;
    //    private String orderStatusInKaspi;
    private OrderStateInStore orderStatus;
    private String descriptionOfOrderStatus;
    private OrderAvailableAction orderAvailableAction;
    private Double price;
    private Integer productsCount;

    public void setOrderStatus(OrderStateInStore orderStatus) {
        this.orderStatus = orderStatus;
        this.orderAvailableAction = orderStatus.getOrderAvailableAction();
        this.descriptionOfOrderStatus = orderStatus.getDescription();
    }
}
