package kz.wonder.wonderuserrepository.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class ProductPriceChangeRequest {
    private List<Price> priceList;
    private List<MainPrice> mainPriceList;


    @Data
    public static class MainPrice {
        private Long productId;
        private Long mainCityId;
    }


    @Data
    public static class Price {
        private Double price;
        private Long cityId;
        private Long productId;
    }
}
