package kz.wonder.wonderuserrepository.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import kz.wonder.wonderuserrepository.entities.WonderUser;
import lombok.Data;

import java.util.List;


@Data
public class KaspiStoreCreateRequest {
    @JsonIgnore
    private WonderUser wonderUser;
    private String kaspiId;
    private Long cityId;
    private String streetName;
    private String streetNumber;
    private String town;
    private String district;
    private String building;
    private String apartment;
    private Long latitude;
    private Long longitude;


    @NotNull(message = "Day of week must be not null")
    @Size(min = 1, max = 7, message = "Number of days should be between 1 and 7")
    private List<DayOfWeekWork> dayOfWeekWorks;
}
