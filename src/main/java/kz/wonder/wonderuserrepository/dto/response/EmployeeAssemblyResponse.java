package kz.wonder.wonderuserrepository.dto.response;

import kz.wonder.wonderuserrepository.dto.enums.AssemblyMode;
import kz.wonder.wonderuserrepository.dto.enums.DeliveryMode;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EmployeeAssemblyResponse {
    private Long orderId;
    private String orderCode;
    private DeliveryMode deliveryMode;
    private AssemblyMode assemblyMode;
    private LocalDateTime deliveryDate;
    private LocalDateTime orderDate;
    private String shopName;
    private Integer productsCount;
}
