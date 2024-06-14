package kz.wonder.wonderuserrepository.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class SellerSupplyReport {
    private Long supplyId;

    private LocalDateTime supplyCreationDate;
    private LocalDateTime supplySelectedDate;
    private LocalDateTime supplyDeliveredDate;
    private LocalDateTime supplyAcceptanceDate;

    private String formattedAddress;
    private List<SupplyBoxInfo> supplyBoxInfo;

    @Data
    public static class SupplyBoxInfo {
        private String boxVendorCode;
        private String boxDescription;
        private String boxName;
        private int size;

        private List<SupplyProductInfo> productInfo;
    }


    @Data
    public static class SupplyProductInfo {
        private String productName;
        private Long productCount;
    }
}
