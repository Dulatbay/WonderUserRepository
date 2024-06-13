package kz.wonder.wonderuserrepository.dto.request;


import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class EmployeeUpdateRequest {
    @Size(min = 10, max = 11, message = "Phone number must be in range 10-11")
    private String phoneNumber;

    @NotNull(message = "Store id must not be null")
    private Long storeId;

    @NotNull(message = "Please provide a first name")
    private String firstName;

    @NotNull(message = "Please provide a last name")
    private String lastName;

    @Email(message = "Please provide a valid email address")
    private String email;
}
