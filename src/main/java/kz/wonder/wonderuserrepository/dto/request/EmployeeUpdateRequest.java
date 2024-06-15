package kz.wonder.wonderuserrepository.dto.request;


import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class EmployeeUpdateRequest {
    @Size(min = 10, max = 20, message = "{requests.employee-update-request.phone-number-must-be-in-range-10-20}")
    private String phoneNumber;

    @NotNull(message = "{requests.employee-update-request.store-id-must-not-be-null}")
    private Long storeId;

    @NotNull(message = "{requests.employee-update-request.please-provide-a-first-name}")
    private String firstName;

    @NotNull(message = "{requests.employee-update-request.please-provide-a-last-name}")
    private String lastName;

    @Email(message = "{requests.employee-update-request.please-provide-a-valid-email-address}")
    private String email;
}
