package kz.wonder.wonderuserrepository.dto.params;

import kz.wonder.wonderuserrepository.dto.enums.OrderBaseStatus;
import kz.wonder.wonderuserrepository.entities.enums.DeliveryMode;
import lombok.Data;

@Data
public class OrderSearchParams {
    private String searchValue = "";
    private DeliveryMode deliveryMode;
    private OrderBaseStatus orderBaseStatus;
    private boolean byOrderCode;
    private boolean byShopName;
    private boolean byStoreAddress;
    private boolean byProductName;
    private boolean byProductArticle;
    private boolean byProductVendorCode;
}
