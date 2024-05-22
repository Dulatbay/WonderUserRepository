package kz.wonder.wonderuserrepository.dto.response;

import kz.wonder.wonderuserrepository.entities.DeliveryMode;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EmployeeAssemblyResponse {
    private Long orderId;
    private String orderCode;
    private DeliveryMode deliveryMode;
    private LocalDateTime deliveryDate;
    private LocalDateTime orderDate;
    private String shopName;
    private Integer productsCount;
}
