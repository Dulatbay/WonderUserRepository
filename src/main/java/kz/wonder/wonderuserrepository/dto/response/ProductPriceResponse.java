package kz.wonder.wonderuserrepository.dto.response;


import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
public class ProductPriceResponse {
    // cities

    // products[i] -> {
    //      productInfo
    // }

    // prices[i] -> {
    //      productId,
    //      cityId,
    //      cityName
    //      price
    // }

    private List<CityResponse> cities;
    private List<ProductInfo> products;

    @Data
    @Builder
    public static class ProductInfo {
        private Long id;
        private String vendorCode;
        private String name;
        private Long count;
        private boolean isPublished;
        private List<ProductPrice> prices;
        private Long mainPriceCityId;
        // todo: remake
    }

    @Data
    public static class ProductPrice {
        private Long cityId;
        private String cityName;
        private Double price;
        private Long count;
    }
}
