package kz.wonder.wonderuserrepository.dto.response;

import lombok.Data;

@Data
public class OrderDetailResponse {
    private String productArticle;
    private String productName;
    private String productVendorCode;
    private String cellCode;
    private Double productTradePrice;
    private Double productSellPrice;
    private Double income;
}
