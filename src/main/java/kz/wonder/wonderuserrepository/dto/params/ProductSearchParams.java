package kz.wonder.wonderuserrepository.dto.params;

import lombok.Data;

@Data
public class ProductSearchParams {
    private String searchValue = "";
    private boolean byVendorCode;
    private boolean byProductName;
}
