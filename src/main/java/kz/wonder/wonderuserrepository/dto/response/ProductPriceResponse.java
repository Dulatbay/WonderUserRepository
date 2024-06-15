package kz.wonder.wonderuserrepository.dto.response;


import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ProductPriceResponse {
    private Content content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean last;

    @Data
    public static class Content {
        private List<CityResponse> cities = new ArrayList<>();
        private List<ProductInfo> products = new ArrayList<>();

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
        }

        @Data
        public static class ProductPrice {
            private Long cityId;
            private String cityName;
            private Double price;
            private Long count;
        }
    }
}
