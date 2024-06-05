package kz.wonder.wonderuserrepository.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AssembleProductResponse {
    private String waybill;
    private AssembleProcessResponse assembleProcessResponse;
}
