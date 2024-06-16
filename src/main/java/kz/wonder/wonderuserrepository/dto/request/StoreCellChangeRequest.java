package kz.wonder.wonderuserrepository.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StoreCellChangeRequest {
    @NotNull(message = "{requests.store-cell-change-request.row-cannot-be-null}")
    private Long row;

    @NotNull(message = "{requests.store-cell-change-request.column-cannot-be-null}")
    private Long col;

    @NotNull(message = "{requests.store-cell-change-request.cell-cannot-be-null}")
    private Long cell;

    @NotNull(message = "{requests.store-cell-change-request.comment-cannot-be-null}")
    private String comment;

    @NotNull(message = "{requests.store-cell-change-request.width-cannot-be-null}")
    private Double width;

    @NotNull(message = "{requests.store-cell-change-request.height-cannot-be-null}")
    private Double height;

    @NotNull(message = "{requests.store-cell-change-request.depth-cannot-be-null}")
    private Double depth;
}
