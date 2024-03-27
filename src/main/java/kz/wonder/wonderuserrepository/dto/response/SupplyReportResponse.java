package kz.wonder.wonderuserrepository.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class SupplyReportResponse {
	private UUID productArticle;
	private String productName;
	private Long countOfProductAccepted;
	private Long countOfProductDeclined;
	private Long countOfProductPending;
}
