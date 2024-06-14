package kz.wonder.wonderuserrepository.dto.response;

import kz.wonder.wonderuserrepository.entities.ProductStateInStore;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ProductStorageResponse {
    List<Product> products;
    private Long supplyId;
    private Long storeId;
    private String pathToSupplyReport;
    private String storeAddress;

    @Data
    @Builder
    public static class Product {
        private String article;
        private String name;
        private String vendorCode;
        private String vendorCodeOfBox;
        private String typeOfBoxName;
        private String pathToBoxBarcode;
        private String pathToProductBarcode;
        private ProductStateInStore productStateInStore;
    }
}
