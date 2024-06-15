package kz.wonder.wonderuserrepository.dto.request;

import jakarta.validation.constraints.NotNull;
import kz.wonder.wonderuserrepository.entities.SupplyState;
import lombok.Data;

@Data
public class SupplyStateToRejectRequest {
    @NotNull(message = "Supply id must not be null")
    private Long supplyId;

    @NotNull(message = "Напишите статус поставки")
    private SupplyState supplyState;
}
