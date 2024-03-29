package kz.wonder.wonderuserrepository.dto.request;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class SupplyCreateRequest {
	private Long storeId;
	private LocalDateTime selectedTime;
	private List<SelectedBox> selectedBoxes;

	@Data
	public static class SelectedBox {
		private Long selectedBoxId;
		private List<Long> productIds;
	}
}
