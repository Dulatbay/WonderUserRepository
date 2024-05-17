package kz.wonder.wonderuserrepository.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StoreCellChangeRequest {
    @NotNull(message = "Row cannot be null")
    private Long row;

    @NotNull(message = "Column cannot be null")
    private Long col;

    @NotNull(message = "Cell cannot be null")
    private Long cell;

    @NotNull(message = "Comment cannot be null")
    private String comment;

    @NotNull(message = "Width cannot be null")
    private Double width;

    @NotNull(message = "Height cannot be null")
    private Double height;

    @NotNull(message = "Depth cannot be null")
    private Double depth;
}
