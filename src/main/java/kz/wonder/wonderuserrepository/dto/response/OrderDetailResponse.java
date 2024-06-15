package kz.wonder.wonderuserrepository.dto.response;

import kz.wonder.wonderuserrepository.dto.enums.OrderAvailableAction;
import kz.wonder.wonderuserrepository.dto.enums.OrderStateInStore;
import lombok.Data;

@Data
public class OrderDetailResponse {
    private String productArticle;
    private String productName;
    private String productVendorCode;
    private String cellCode;
    private Double productTradePrice;
    private Double productSellPrice;
    private String pathToProductBarcode;
    private String pathToBoxBarcode;
    private Double income;
    private OrderStateInStore orderStatus;
    private String descriptionOfOrderStatus;
    private OrderAvailableAction orderAvailableAction;

    public void setOrderStatus(OrderStateInStore orderStatus) {
        this.orderStatus = orderStatus;
        this.orderAvailableAction = orderStatus.getOrderAvailableAction();
        this.descriptionOfOrderStatus = orderStatus.getDescription();
    }
}
