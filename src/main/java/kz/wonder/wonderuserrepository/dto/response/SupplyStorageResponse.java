package kz.wonder.wonderuserrepository.dto.response;

import kz.wonder.wonderuserrepository.entities.SupplyState;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class SupplyStorageResponse {
	private LocalDate date;
	private List<Supply> supplies;
	@Data
	public static class Supply {
		private Long supplyId;
		private String sellerName;
		private Long sellerId;
		private SupplyState supplyState;
	}
}
