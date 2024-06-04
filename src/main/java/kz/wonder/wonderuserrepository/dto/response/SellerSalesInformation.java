package kz.wonder.wonderuserrepository.dto.response;

import lombok.Data;

@Data
public class SellerSalesInformation {
    private StateInfo<Long> ordersInfo;
    private StateInfo<Long> suppliesInfo;
    private StateInfo<Long> productsInfo;
    private StateInfo<Double> incomeInfo;
}
