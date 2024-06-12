package kz.wonder.wonderuserrepository.dto.response;


import lombok.Data;

@Data
public class AdminLastOrdersInformation {
    private String orderCode;
    private String shopName;
    private Double price;
}
