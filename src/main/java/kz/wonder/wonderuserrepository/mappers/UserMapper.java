package kz.wonder.wonderuserrepository.mappers;

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

    public SellerUserResponse toUserResponse(WonderUser wonderUser, UserRepresentation userRepresentation, KaspiToken kaspiToken) {
        SellerUserResponse sellerUserResponse = new SellerUserResponse();
        sellerUserResponse.setId(wonderUser.getId());
        sellerUserResponse.setSellerName(kaspiToken.getSellerName());
        sellerUserResponse.setSellerId(kaspiToken.getSellerId());
        sellerUserResponse.setEmail(userRepresentation.getEmail());
        sellerUserResponse.setFirstName(userRepresentation.getFirstName());
        sellerUserResponse.setLastName(userRepresentation.getLastName());
        sellerUserResponse.setPhoneNumber(wonderUser.getPhoneNumber());
        sellerUserResponse.setTokenKaspi(kaspiToken.getToken());
        sellerUserResponse.setPathToXml(pathToFile + kaspiToken.getPathToXml());
        sellerUserResponse.setXmlUpdatedAt(kaspiToken.getXmlUpdatedAt());

        return sellerUserResponse;
    }
}
