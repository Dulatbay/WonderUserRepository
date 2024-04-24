package kz.wonder.wonderuserrepository.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EmployeeOrderResponse {
    private String orderCode;
    private LocalDateTime orderCreatedAt;
    private String deliveryType;
    private LocalDateTime orderToSendTime;
    private String orderStatus;
}
