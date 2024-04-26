package kz.wonder.wonderuserrepository.dto.response;

import kz.wonder.wonderuserrepository.entities.SupplyState;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class SupplyStorageResponse {
    private LocalDate date;
    private List<Supply> supplies;

    @Data
    @Builder
    public static class Supply {
        private Long supplyId;
        private String sellerName;
        private Long sellerId;
        private SupplyState supplyState;
    }
}
