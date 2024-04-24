package kz.wonder.wonderuserrepository.dto.response;


import lombok.Data;

@Data
public class OrderEmployeeDetailResponse {
    private String orderName;
    private String orderArticle;
    private String orderVendorCode;
    private String orderCell;
}
