package kz.wonder.wonderuserrepository.dto.base;

import kz.wonder.wonderuserrepository.dto.enums.OrderAvailableAction;
import kz.wonder.wonderuserrepository.dto.enums.OrderStateInStore;
import lombok.Data;

@Data
public abstract class OrderWithStatus {
    private OrderStateInStore orderStatus;
    private String descriptionOfOrderStatus;
    private OrderAvailableAction orderAvailableAction;

    public void setOrderStatus(OrderStateInStore orderStatus) {
        this.orderStatus = orderStatus;
        this.orderAvailableAction = orderStatus.getOrderAvailableAction();
        this.descriptionOfOrderStatus = orderStatus.getDescription();
    }
}
