package kz.wonder.wonderuserrepository.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class SupplyScanRequest {
    private Long supplyId;
    private List<ProductCell> productCells;

    @Data
    public static class ProductCell {
        private String cellCode;
        List<String> productArticles;
    }
}
