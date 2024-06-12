package kz.wonder.wonderuserrepository.dto.params;

import lombok.Data;

@Data
public class ProductSearchParams {
    private String searchValue;
    private boolean byArticle;
    private boolean byVendorCode;
    private boolean byProductName;
    private boolean byShopName;
    private boolean byCellCode;
}
