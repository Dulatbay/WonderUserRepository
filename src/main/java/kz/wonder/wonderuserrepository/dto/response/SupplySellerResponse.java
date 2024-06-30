package kz.wonder.wonderuserrepository.dto.response;

import kz.wonder.wonderuserrepository.entities.enums.SupplyState;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class SupplySellerResponse {
    private Long id;
    private LocalDateTime supplyCreatedTime;
    private LocalDateTime supplyAcceptTime;
    private LocalDateTime supplySelectedTime;
    private SupplyState supplyState;
    private String pathToReport;
    private String formattedAddress;
    private String pathToPAO;
}
