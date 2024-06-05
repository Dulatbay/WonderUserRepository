package kz.wonder.wonderuserrepository.dto.request;


import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EmployeeUpdateRequest {
    @Digits(integer = 10, fraction = 0, message = "Phone number must have 10 integers")
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
