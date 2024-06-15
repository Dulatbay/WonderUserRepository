package kz.wonder.wonderuserrepository.mappers;

import kz.wonder.wonderuserrepository.dto.request.SupplyCreateRequest;
import kz.wonder.wonderuserrepository.dto.response.ProductStorageResponse;
import kz.wonder.wonderuserrepository.dto.response.SupplyAdminResponse;
import kz.wonder.wonderuserrepository.dto.response.SupplyProductResponse;
import kz.wonder.wonderuserrepository.dto.response.SupplySellerResponse;
import kz.wonder.wonderuserrepository.entities.*;
import kz.wonder.wonderuserrepository.entities.enums.SupplyState;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

import static kz.wonder.wonderuserrepository.constants.ValueConstants.*;

@Component
public class SupplyMapper {
    @Value("${application.file-api.url}")
    private String fileApiUrl;

    public Supply toSupplyEntity(SupplyCreateRequest createRequest, WonderUser user, KaspiStore store) {
        Supply supply = new Supply();
        supply.setAuthor(user);
        supply.setKaspiStore(store);
        supply.setSupplyState(SupplyState.START);
        supply.setSupplyBoxes(new ArrayList<>());
        supply.setSelectedTime(createRequest.getSelectedTime());
        return supply;
    }

    public SupplyAdminResponse toSupplyAdminResponse(Supply supply, String userId, String fullName) {
        SupplyAdminResponse supplyAdminResponse = new SupplyAdminResponse();
        supplyAdminResponse.setId(supply.getId());
        supplyAdminResponse.setSupplyState(supply.getSupplyState());
        supplyAdminResponse.setSupplyAcceptTime(supply.getAcceptedTime());
        supplyAdminResponse.setSupplyCreatedTime(supply.getCreatedAt());
        supplyAdminResponse.setPathToReport(getPathToReport(supply));
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
        supplyProductResponse.setPathToProductBarcode(getPathToProductBarcode(supplyBoxProduct));
        supplyProductResponse.setPathToBoxBarcode(getPathToBoxBarcode(supplyBox));


        return supplyProductResponse;
    }

    public SupplySellerResponse toSupplySellerResponse(Supply supply) {
        return SupplySellerResponse.builder()
                .supplyCreatedTime(supply.getCreatedAt())
                .supplyAcceptTime(supply.getAcceptedTime())
                .supplyState(supply.getSupplyState())
                .pathToReport(getPathToReport(supply))
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
                .pathToBoxBarcode(getPathToBoxBarcode(supplyBox))
                .pathToProductBarcode(getPathToProductBarcode(supplyBoxProduct))
                .build();
    }

    public ProductStorageResponse toProductStorageResponse(Supply supply) {
        return ProductStorageResponse.builder()
                .storeId(supply.getKaspiStore().getId())
                .supplyId(supply.getId())
                .products(buildProducts(supply))
                .storeAddress(supply.getKaspiStore().getFormattedAddress())
                .pathToSupplyReport(getPathToReport(supply))
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

    private String getPathToReport(Supply supply) {
        return fileApiUrl + "/" + FILE_MANAGER_SUPPLY_REPORT_DIR + "/retrieve/files/supply_report_" + supply.getId() + ".pdf";
    }

    private String getPathToProductBarcode(SupplyBoxProduct supplyBoxProduct) {
        return fileApiUrl + "/" + FILE_MANAGER_PRODUCT_BARCODE_DIR + "/retrieve/files/" + supplyBoxProduct.getPathToBarcode();
    }

    private String getPathToBoxBarcode(SupplyBox supplyBox) {
        return fileApiUrl + "/" + FILE_MANAGER_BOX_BARCODE_DIR + "/retrieve/files/" + supplyBox.getPathToBarcode();
    }

}
