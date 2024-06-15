package kz.wonder.wonderuserrepository.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class SupplyScanRequest {
    @NotNull(message = "{requests.supply-scan-request.supply-id-must-not-be-null}")
    private Long supplyId;

    private List<ProductCell> productCells;

    @Data
    public static class ProductCell {
        @NotNull(message = "{requests.supply-scan-request.cell-code-must-not-be-null}")
        private String cellCode;

        List<String> productArticles;
    }
}
