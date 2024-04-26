package kz.wonder.wonderuserrepository.dto.response;

import kz.wonder.wonderuserrepository.entities.SupplyState;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class SupplySellerResponse {
    private Long id;
    private LocalDateTime supplyCreatedTime;
    private LocalDateTime supplyAcceptTime;
    private SupplyState supplyState;
}
