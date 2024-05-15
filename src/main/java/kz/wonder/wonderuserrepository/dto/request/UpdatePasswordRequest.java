package kz.wonder.wonderuserrepository.dto.request;


import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

@Data
public class UpdatePasswordRequest {
    @JsonIgnore
    private String email;
    private String oldPassword;
    private String newPassword;
}
