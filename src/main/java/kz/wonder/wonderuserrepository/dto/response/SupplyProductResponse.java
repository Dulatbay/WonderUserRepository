package kz.wonder.wonderuserrepository.dto.response;

import lombok.Data;

@Data
public class SupplyProductResponse {
    private String article;
    private String name;
    private String vendorCode;
    private String boxBarCode;
    private String boxTypeName;
    private String storeAddress;
    private String shopName;
    private String pathToBoxBarcode;
    private String pathToProductBarcode;
}
