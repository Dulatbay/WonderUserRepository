package kz.wonder.wonderuserrepository.dto.request;

import lombok.Data;

@Data
public class StoreCellChangeRequest {
    private Long row;
    private Long col;
    private Long cell;
    private String comment;
    private Double width;
    private Double height;
    private Double depth;
}
