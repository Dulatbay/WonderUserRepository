package kz.wonder.wonderuserrepository.mappers;

import kz.wonder.wonderuserrepository.dto.request.SupplyCreateRequest;
import kz.wonder.wonderuserrepository.dto.response.SellerSupplyReport;
import kz.wonder.wonderuserrepository.dto.response.SupplyAdminResponse;
import kz.wonder.wonderuserrepository.dto.response.SupplyProductResponse;
import kz.wonder.wonderuserrepository.entities.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class SupplyMapper {
    public Supply toSupply(SupplyCreateRequest createRequest, WonderUser user, KaspiStore store) {
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
        supplyAdminResponse.setSeller(new SupplyAdminResponse.Seller(userId, fullName));
        return supplyAdminResponse;
    }

    public SupplyProductResponse toSupplyProductResponse(Supply supply, SupplyBox supplyBox, SupplyBoxProduct supplyBoxProducts) {
        SupplyProductResponse supplyProductResponse = new SupplyProductResponse();

        Product product = supplyBoxProducts.getProduct();
        String shopName = supply.getAuthor().getKaspiToken().getSellerName();

        supplyProductResponse.setName(product.getName());
        supplyProductResponse.setArticle(supplyBoxProducts.getArticle());
        supplyProductResponse.setVendorCode(product.getVendorCode());
        supplyProductResponse.setBoxBarCode(supplyBox.getVendorCode());
        supplyProductResponse.setStoreAddress(supply.getKaspiStore().getFormattedAddress());
        supplyProductResponse.setBoxTypeName(supplyBox.getBoxType().getName());
        supplyProductResponse.setShopName(shopName);

        return supplyProductResponse;
    }

}
