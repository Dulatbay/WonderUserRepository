package kz.wonder.wonderuserrepository.dto.response;

import kz.wonder.wonderuserrepository.entities.SupplyStates;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SupplyAdminResponse {
	private Long id;
	private Seller seller;
	private LocalDateTime supplyCreatedTime;
	private LocalDateTime supplyAcceptTime;
	private SupplyStates supplyStates;
	public record Seller(String keycloakId, String fullName) {}
}
