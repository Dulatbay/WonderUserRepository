package kz.wonder.wonderuserrepository.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class StoreCellCreateRequest {
    @NotNull(message = "Please provide store id")
    @Positive(message = "Store id must be positive")
    private Long storeId;

    @NotNull(message = "Row cannot be null")
    private Long row;

    @NotNull(message = "Column cannot be null")
    private Long col;

    @NotNull(message = "Cell cannot be null")
    private Long cell;

    @NotNull(message = "Please provide comment")
    private String comment;

    @NotNull(message = "Please provide width")
    private Double width;

    @NotNull(message = "Please provide height")
    private Double height;

    @NotNull(message = "Please provide Depth")
    private Double depth;
}
