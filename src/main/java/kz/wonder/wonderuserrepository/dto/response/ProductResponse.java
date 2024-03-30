package kz.wonder.wonderuserrepository.dto.response;


import lombok.Builder;

import java.util.List;

@Builder
public record ProductResponse(Long id,
                              String vendorCode,
                              String keycloakUserId,
                              String name,
                              boolean enabled,
                              List<ProductPriceResponse> prices) {
	@Builder
	public record ProductPriceResponse(String cityName, Double price) {

	}
}
