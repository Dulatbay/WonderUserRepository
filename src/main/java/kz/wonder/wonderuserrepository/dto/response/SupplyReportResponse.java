package kz.wonder.wonderuserrepository.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SupplyReportResponse {
	private String productBarcode;
	private String productName;
	private Long countOfProductAccepted;
	private Long countOfProductDeclined;
	private Long countOfProductPending;
}
