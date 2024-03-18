package kz.wonder.wonderuserrepository.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import kz.wonder.wonderuserrepository.entities.User;
import lombok.Data;

import java.util.List;


@Data
public class KaspiStoreCreateRequest {
    @JsonIgnore
    private User user;
    private String kaspiId;
    private Long cityId;
    private String street;
    private String apartment;
    private List<DayOfWeekWork> dayOfWeekWorks;
    public record DayOfWeekWork(Integer numericDayOfWeek, String openTime, String closeTime) { }
}
