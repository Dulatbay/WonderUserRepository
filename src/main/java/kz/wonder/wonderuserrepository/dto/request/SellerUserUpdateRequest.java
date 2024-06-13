package kz.wonder.wonderuserrepository.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SellerUserUpdateRequest {
    @NotNull(message = "Phone number not found")
    @Size(min = 10, max = 20, message = "Phone number must be in range 10-20")
    private String phoneNumber;

    @NotNull(message = "Please provide seller name")
    private String sellerName;

    @NotNull(message = "Please provide seller id")
    private String sellerId;

    @NotNull(message = "Please provide kaspi token ")
    private String tokenKaspi;

    @NotNull(message = "Please provide first name ")
    private String firstName;

    @NotNull(message = "Please provide last name ")
    private String lastName;

    @NotNull(message = "Please provide email ")
    @Email(message = "Введите валидную почту")
    private String email;
}
