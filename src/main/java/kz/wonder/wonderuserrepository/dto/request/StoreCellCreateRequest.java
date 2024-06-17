package kz.wonder.wonderuserrepository.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class StoreCellCreateRequest {
    @NotNull(message = "{requests.store-cell-create-request.please-provide-store-id}")
    @Positive(message = "{requests.store-cell-create-request.store-id-must-be-positive}")
    private Long storeId;

    @NotNull(message = "{requests.store-cell-create-request.row-cannot-be-null}")
    private Long row;

    @NotNull(message = "{requests.store-cell-create-request.column-cannot-be-null}")
    private Long col;

    @NotNull(message = "{requests.store-cell-create-request.cell-cannot-be-null}")
    private Long cell;

    private String comment;

    @NotNull(message = "{requests.store-cell-create-request.please-provide-width}")
    private Double width;

    @NotNull(message = "{requests.store-cell-create-request.please-provide-height}")
    private Double height;

    @NotNull(message = "{requests.store-cell-create-request.please-provide-depth}")
    private Double depth;
}
