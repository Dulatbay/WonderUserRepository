package kz.wonder.wonderuserrepository.dto.response;

public record SellerUserResponse(String keycloakId,
                                 boolean enabled,
                                 String token,
                                 boolean xmlUpdated,
                                 String pathToXml,
                                 String sellerName) {
}
