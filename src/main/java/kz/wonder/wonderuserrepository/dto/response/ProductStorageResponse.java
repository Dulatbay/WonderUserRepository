package kz.wonder.wonderuserrepository.dto.response;

import kz.wonder.wonderuserrepository.entities.ProductStateInStore;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class ProductStorageResponse {
	List<Product> products;
	private Long supplyId;
	private Long storeId;
	private String storeAddress;

	@Data
	@Builder
	public static class Product {
		private UUID article;
		private String name;
		private String vendorCode;
		private UUID vendorCodeOfBox;
		private String typeOfBoxName;
		private ProductStateInStore productStateInStore;
	}
}
