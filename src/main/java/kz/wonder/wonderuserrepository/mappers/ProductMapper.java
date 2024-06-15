package kz.wonder.wonderuserrepository.mappers;

import kz.wonder.wonderuserrepository.dto.response.ProductPriceResponse;
import kz.wonder.wonderuserrepository.dto.response.ProductResponse;
import kz.wonder.wonderuserrepository.dto.response.ProductSearchResponse;
import kz.wonder.wonderuserrepository.dto.response.ProductWithSize;
import kz.wonder.wonderuserrepository.entities.*;
import kz.wonder.wonderuserrepository.repositories.KaspiTokenRepository;
import kz.wonder.wonderuserrepository.repositories.ProductSizeRepository;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductMapper {
    private final ProductSizeRepository productSizeRepository;
    private final KaspiTokenRepository kaspiTokenRepository;
    private final MessageSource messageSource;

    public ProductResponse mapToResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .enabled(product.isEnabled())
                .name(product.getName())
                .vendorCode(product.getVendorCode())
                .keycloakUserId(product.getKeycloakId())
                .mainPriceCityId(product.getMainCityPrice() == null ? null : product.getMainCityPrice().getId())
                .counts(product.getPrices().stream().map(price -> {
                    var city = price.getKaspiCity();
                    var count = (product.getSupplyBoxes()
                            .stream()
                            .filter(p ->
                                    p.getState() == ProductStateInStore.ACCEPTED
                                            && p.getSupplyBox().getSupply().getKaspiStore().getKaspiCity().getId().equals(city.getId())
                            )
                            .count());

                    return new ProductResponse.ProductCount(city.getName(), count);
                }).toList())

                .build();
    }

    public ProductWithSize mapProductsSizesResponse(SupplyBoxProduct supplyBoxProduct) {
        final var product = supplyBoxProduct.getProduct();
        final var size = productSizeRepository.findByOriginVendorCode(product.getOriginalVendorCode())
                .orElse(new ProductSize());

        ProductWithSize productWithSize = new ProductWithSize();
        productWithSize.setProductName(product.getName());
        productWithSize.setWeight(size.getWeight());
        productWithSize.setHeight(size.getHeight());
        productWithSize.setLength(size.getLength());
        productWithSize.setWidth(size.getWidth());
        productWithSize.setComment(size.getComment());
        productWithSize.setVendorCode(product.getVendorCode());
        productWithSize.setState(supplyBoxProduct.getState());


        return productWithSize;
    }

    public ProductSearchResponse mapProductSearchResponse(SupplyBoxProduct supplyBoxProduct) {
        var product = supplyBoxProduct.getProduct();
        var token = kaspiTokenRepository.findByWonderUserKeycloakId(product.getKeycloakId())
                .orElseThrow(() -> new IllegalArgumentException(messageSource.getMessage("mappers.product-mapper.the-user-may-have-been-deleted", null, LocaleContextHolder.getLocale())));
        var storeCellProduct = supplyBoxProduct.getStoreCellProduct();

        ProductSearchResponse productSearchResponse = new ProductSearchResponse();
        productSearchResponse.setProductId(product.getId());
        productSearchResponse.setProductName(product.getName());
        productSearchResponse.setPrice(product.getTradePrice());
        productSearchResponse.setVendorCode(product.getVendorCode());
        productSearchResponse.setShopName(token.getSellerName());
        productSearchResponse.setCellCode(storeCellProduct == null ? "Еще не принят" : storeCellProduct.getStoreCell().getCode());
        productSearchResponse.setArticle(supplyBoxProduct.getArticle());

        return productSearchResponse;
    }

    public static @NotNull ProductPriceResponse.ProductPrice mapProductPrice(Product product, ProductPrice price, KaspiCity city) {
        var productPrice = new ProductPriceResponse.ProductPrice();

        // todo: сделал поставку в город, где не указана цена

        productPrice.setCityId(city.getId());
        productPrice.setCityName(city.getName());
        productPrice.setCount(product.getSupplyBoxes()
                .stream()
                .filter(p ->
                        p.getState() == ProductStateInStore.ACCEPTED
                                && p.getSupplyBox().getSupply().getKaspiStore().getKaspiCity().getId().equals(city.getId())
                )
                .count());
        productPrice.setPrice(price.getPrice());
        return productPrice;
    }

}
