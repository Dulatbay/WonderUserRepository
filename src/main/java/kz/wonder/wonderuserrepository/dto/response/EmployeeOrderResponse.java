package kz.wonder.wonderuserrepository.dto.response;

import kz.wonder.wonderuserrepository.dto.base.OrderWithStatus;
import kz.wonder.wonderuserrepository.entities.enums.DeliveryMode;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EmployeeOrderResponse extends OrderWithStatus {
    private String orderCode;
    private String shopName;
    private String formattedAddress;
    private LocalDateTime orderCreatedAt;
    private LocalDateTime orderToSendTime;
    private DeliveryMode deliveryType;
    //    private String orderStatusInKaspi;
    private Double price;
    private Integer productsCount;

}
