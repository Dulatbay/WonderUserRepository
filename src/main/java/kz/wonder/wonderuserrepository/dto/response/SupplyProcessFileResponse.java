package kz.wonder.wonderuserrepository.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SupplyProcessFileResponse {
    private Long productId;
    private String vendorCode;
    private String name;
    private Long quantity;
}
