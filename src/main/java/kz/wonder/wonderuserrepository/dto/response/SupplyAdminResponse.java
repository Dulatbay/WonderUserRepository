package kz.wonder.wonderuserrepository.dto.response;

import kz.wonder.wonderuserrepository.entities.SupplyState;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SupplyAdminResponse {
    private Long id;
    private Seller seller;
    private LocalDateTime supplyCreatedTime;
    private LocalDateTime supplyAcceptTime;
    private SupplyState supplyState;
    private String pathToReport;

    public record Seller(String keycloakId, String fullName) {
    }
}
