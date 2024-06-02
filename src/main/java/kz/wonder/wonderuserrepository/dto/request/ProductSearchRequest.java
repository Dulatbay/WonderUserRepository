package kz.wonder.wonderuserrepository.dto.request;

import lombok.Data;

@Data
public class ProductSearchRequest {
    private String searchValue;
    private boolean byArticle;
    private boolean byVendorCode;
    private boolean byProductName;
    private boolean byShopName;
    private boolean byCellCode;
}
