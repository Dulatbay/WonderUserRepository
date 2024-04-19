package kz.wonder.wonderuserrepository.dto.response;

import lombok.Data;

@Data
public class CityResponse {
    private Long id;
    private String name;
    private String code;
    private boolean enabled;
}
