package kz.wonder.wonderuserrepository.dto.response;

import lombok.Data;

@Data
public class AdminSalesInformation {
    private StateInfo<Long> ordersInfo;
    private StateInfo<Integer> sellersInfo;
    private StateInfo<Long> suppliesInfo;
    private StateInfo<Double> incomeInfo;
}
