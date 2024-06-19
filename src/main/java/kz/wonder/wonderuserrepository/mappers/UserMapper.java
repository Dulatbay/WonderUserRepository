package kz.wonder.wonderuserrepository.mappers;

import kz.wonder.wonderuserrepository.dto.response.SellerUserDetailResponse;
import kz.wonder.wonderuserrepository.dto.response.SellerUserResponse;
import kz.wonder.wonderuserrepository.entities.KaspiToken;
import kz.wonder.wonderuserrepository.entities.WonderUser;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    private final String pathToFile;

    public UserMapper(@Value("${application.file-api.url}") String fileApiUrl) {
        this.pathToFile = fileApiUrl + "/xml/retrieve/files/";
    }

    public SellerUserDetailResponse toUserDetailResponse(WonderUser wonderUser, UserRepresentation userRepresentation, KaspiToken kaspiToken) {
        SellerUserDetailResponse sellerUserDetailResponse = new SellerUserDetailResponse();
        sellerUserDetailResponse.setId(wonderUser.getId());
        sellerUserDetailResponse.setSellerName(kaspiToken.getSellerName());
        sellerUserDetailResponse.setSellerId(kaspiToken.getSellerId());
        sellerUserDetailResponse.setEmail(userRepresentation.getEmail());
        sellerUserDetailResponse.setFirstName(userRepresentation.getFirstName());
        sellerUserDetailResponse.setLastName(userRepresentation.getLastName());
        sellerUserDetailResponse.setPhoneNumber(wonderUser.getPhoneNumber());
        sellerUserDetailResponse.setTokenKaspi(kaspiToken.getToken());
        sellerUserDetailResponse.setPathToXml(pathToFile + kaspiToken.getPathToXml());
        sellerUserDetailResponse.setXmlUpdatedAt(kaspiToken.getXmlUpdatedAt());

        return sellerUserDetailResponse;
    }

    public SellerUserResponse toUserResponse(KaspiToken kaspiToken) {
        return new SellerUserResponse(kaspiToken.getWonderUser().getKeycloakId(),
                kaspiToken.isEnabled(),
                kaspiToken.getToken(),
                kaspiToken.isXmlUpdated(),
                kaspiToken.getPathToXml(),
                kaspiToken.getSellerName());
    }
}
