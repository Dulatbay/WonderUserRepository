package kz.wonder.wonderuserrepository.dto.response;

import kz.wonder.wonderuserrepository.entities.ProductStateInStore;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class ProductStorageResponse {
	private Long supplyId;
	private Long storeId;
	private String storeAddress;
	List<Product> products;

	@Data
	@Builder
	public static class Product {
		private String article;
		private String name;
		private String vendorCode;
		private String vendorCodeOfBox;
		private String typeOfBoxName;
		private ProductStateInStore productStateInStore;
	}
}
