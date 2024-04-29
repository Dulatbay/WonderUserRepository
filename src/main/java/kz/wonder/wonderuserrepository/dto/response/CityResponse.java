package kz.wonder.wonderuserrepository.dto.response;

import lombok.Data;

import java.util.Objects;

@Data
public class CityResponse {
    private Long id;
    private String name;
    private String code;
    private boolean enabled;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CityResponse that = (CityResponse) o;
        return enabled == that.enabled &&
                Objects.equals(id, that.id) &&
                Objects.equals(name, that.name) &&
                Objects.equals(code, that.code);
    }
}
