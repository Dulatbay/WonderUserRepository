package kz.wonder.wonderuserrepository.mappers;

import kz.wonder.wonderuserrepository.dto.request.SupplyCreateRequest;
import kz.wonder.wonderuserrepository.dto.response.ProductStorageResponse;
import kz.wonder.wonderuserrepository.dto.response.SupplyAdminResponse;
import kz.wonder.wonderuserrepository.dto.response.SupplyProductResponse;
import kz.wonder.wonderuserrepository.dto.response.SupplySellerResponse;
import kz.wonder.wonderuserrepository.entities.*;
import kz.wonder.wonderuserrepository.entities.enums.SupplyState;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

import static kz.wonder.wonderuserrepository.constants.ValueConstants.*;

@Component
@RequiredArgsConstructor
public class SupplyMapper {
    private final BarcodeMapper barcodeMapper;

    public Supply toSupplyEntity(SupplyCreateRequest createRequest, WonderUser user, KaspiStore store) {
        Supply supply = new Supply();
        supply.setAuthor(user);
        supply.setKaspiStore(store);
        supply.setSupplyState(SupplyState.START);
        supply.setSelectedTime(createRequest.getSelectedTime());
        return supply;
    }

    public SupplyAdminResponse toSupplyAdminResponse(Supply supply, String userId, String fullName) {
        SupplyAdminResponse supplyAdminResponse = new SupplyAdminResponse();
        supplyAdminResponse.setId(supply.getId());
        supplyAdminResponse.setSupplyState(supply.getSupplyState());
        supplyAdminResponse.setSupplyAcceptTime(supply.getAcceptedTime());
        supplyAdminResponse.setSupplyCreatedTime(supply.getCreatedAt());
        supplyAdminResponse.setPathToReport(barcodeMapper.getPathToReport(supply));
        supplyAdminResponse.setSeller(new SupplyAdminResponse.Seller(userId, fullName));
        return supplyAdminResponse;
    }

    public SupplyProductResponse toSupplyProductResponse(Supply supply, SupplyBox supplyBox, SupplyBoxProduct supplyBoxProduct) {
        SupplyProductResponse supplyProductResponse = new SupplyProductResponse();

        Product product = supplyBoxProduct.getProduct();
        String shopName = supply.getAuthor().getKaspiToken().getSellerName();

        supplyProductResponse.setName(product.getName());
        supplyProductResponse.setArticle(supplyBoxProduct.getArticle());
        supplyProductResponse.setVendorCode(product.getVendorCode());
        supplyProductResponse.setBoxBarCode(supplyBox.getVendorCode());
        supplyProductResponse.setStoreAddress(supply.getKaspiStore().getFormattedAddress());
        supplyProductResponse.setBoxTypeName(supplyBox.getBoxType().getName());
        supplyProductResponse.setShopName(shopName);
        supplyProductResponse.setPathToProductBarcode(barcodeMapper.getPathToProductBarcode(supplyBoxProduct));
        supplyProductResponse.setPathToBoxBarcode(barcodeMapper.getPathToBoxBarcode(supplyBox));


        return supplyProductResponse;
    }

    public SupplySellerResponse toSupplySellerResponse(Supply supply) {
        return SupplySellerResponse.builder()
                .supplyCreatedTime(supply.getCreatedAt())
                .supplySelectedTime(supply.getSelectedTime())
                .supplyAcceptTime(supply.getAcceptedTime())
                .supplyState(supply.getSupplyState())
                .pathToReport(barcodeMapper.getPathToReport(supply))
                .id(supply.getId())
                .formattedAddress(supply.getKaspiStore().getFormattedAddress())
                .build();
    }

    private ProductStorageResponse.Product toProductStorageResponse(SupplyBox supplyBox, SupplyBoxProduct supplyBoxProduct) {
        return ProductStorageResponse.Product.builder()
                .article(supplyBoxProduct.getArticle())
                .productStateInStore(supplyBoxProduct.getState())
                .typeOfBoxName(supplyBox.getBoxType().getName())
                .vendorCodeOfBox(supplyBox.getVendorCode())
                .vendorCode(supplyBoxProduct.getProduct().getVendorCode())
                .name(supplyBoxProduct.getProduct().getName())
                .pathToBoxBarcode(barcodeMapper.getPathToBoxBarcode(supplyBox))
                .pathToProductBarcode(barcodeMapper.getPathToProductBarcode(supplyBoxProduct))
                .build();
    }

    public ProductStorageResponse toProductStorageResponse(Supply supply) {
        return ProductStorageResponse.builder()
                .storeId(supply.getKaspiStore().getId())
                .supplyId(supply.getId())
                .products(buildProducts(supply))
                .storeAddress(supply.getKaspiStore().getFormattedAddress())
                .pathToSupplyReport(barcodeMapper.getPathToReport(supply))
                .build();
    }

    private ArrayList<ProductStorageResponse.Product> buildProducts(Supply supply) {
        ArrayList<ProductStorageResponse.Product> products = new ArrayList<>();

        supply.getSupplyBoxes().forEach(supplyBox ->
                supplyBox.getSupplyBoxProducts().forEach(supplyBoxProducts -> {
                    var product = this.toProductStorageResponse(supplyBox, supplyBoxProducts);
                    products.add(product);
                })
        );

        return products;
    }


}
