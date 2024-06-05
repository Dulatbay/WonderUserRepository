package kz.wonder.wonderuserrepository.dto.request;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdatePasswordRequest {
    @JsonIgnore
    private String email;

    @NotNull(message = "Please provide old password")
    private String oldPassword;

    @NotNull(message = "Please provide new password")
    private String newPassword;
}
