package kz.wonder.wonderuserrepository.dto.response;

import lombok.Data;

@Data
public class StoreCellResponse {
    private Long id;
    private Long row;
    private Long col;
    private Long cell;
    private String comment;
    private Double width;
    private Double height;
    private Double depth;
}
