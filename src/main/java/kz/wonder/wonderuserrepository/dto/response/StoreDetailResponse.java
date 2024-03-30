package kz.wonder.wonderuserrepository.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class StoreDetailResponse {
	private Long id;
	private String kaspiId;
	private String address;
	private String street;
	private City city;
	private List<AvailableWorkTime> availableWorkTimes;
	private List<AvailableBoxType> availableBoxTypes;
	private boolean enabled;
	private Long userId;


	@Data
	@Builder
	public static class City {
		private Long id;
		private String name;
	}

	@Data
	@Builder
	public static class AvailableBoxType {
		private Long id;
		private String name;
		private String description;
		private List<String> imageUrls;
	}
}
