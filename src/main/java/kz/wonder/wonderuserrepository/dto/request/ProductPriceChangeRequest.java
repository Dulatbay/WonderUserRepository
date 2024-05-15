package kz.wonder.wonderuserrepository.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class ProductPriceChangeRequest {
    private List<Price> priceList;

    @Data
    public static class Price {
        private Double price;
        private Long cityId;
    }
}
