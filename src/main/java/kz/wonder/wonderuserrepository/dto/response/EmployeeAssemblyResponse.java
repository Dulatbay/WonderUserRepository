package kz.wonder.wonderuserrepository.dto.response;

import kz.wonder.wonderuserrepository.entities.enums.AssembleState;
import kz.wonder.wonderuserrepository.entities.enums.DeliveryMode;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EmployeeAssemblyResponse {
    private Long orderId;
    private String orderCode;
    private DeliveryMode deliveryMode;
    private LocalDateTime deliveryDate;
    private LocalDateTime orderDate;
    private AssembleState assembleState;
    private String shopName;
    private Integer productsCount;
}
