package kz.wonder.wonderuserrepository.dto.request;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdatePasswordRequest {
    @JsonIgnore
    private String email;

    @NotNull(message = "{requests.update-password-request.please-provide-old-password}")
    private String oldPassword;

    @NotNull(message = "{requests.update-password-request.please-provide-new-password}")
    private String newPassword;
}
