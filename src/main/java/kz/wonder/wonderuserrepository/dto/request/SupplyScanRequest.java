package kz.wonder.wonderuserrepository.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class SupplyScanRequest {
    @NotNull(message = "Supply id must not be null")
    private Long supplyId;

    private List<ProductCell> productCells;

    @Data
    public static class ProductCell {
        List<String> productArticles;
        @NotNull(message = "Cell code must not be null")
        private String cellCode;
    }
}
