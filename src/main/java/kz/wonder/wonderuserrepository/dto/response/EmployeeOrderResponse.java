package kz.wonder.wonderuserrepository.dto.response;

import kz.wonder.wonderuserrepository.entities.DeliveryMode;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EmployeeOrderResponse {
    private String orderCode;
    private LocalDateTime orderCreatedAt;
    private DeliveryMode deliveryType;
    private LocalDateTime orderToSendTime;
    private String orderStatus;
}
