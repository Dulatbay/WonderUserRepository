package kz.wonder.wonderuserrepository.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SellerUserUpdateRequest {
    @NotNull(message = "{requests.seller-user-update-request.phone-number-not-found}")
    @Size(min = 10, max = 20, message = "{requests.seller-user-update-request.phone-number-must-be-in-range-10-20}")
    private String phoneNumber;

    @NotNull(message = "{requests.seller-user-update-request.please-provide-seller-name}")
    private String sellerName;

    @NotNull(message = "{requests.seller-user-update-request.please-provide-seller-id}")
    private String sellerId;

    @NotNull(message = "{requests.seller-user-update-request.please-provide-kaspi-token}")
    private String tokenKaspi;

    @NotNull(message = "{requests.seller-user-update-request.please-provide-a-first-name}")
    private String firstName;

    @NotNull(message = "{requests.seller-user-update-request.please-provide-a-last-name}")
    private String lastName;

    @NotNull(message = "{requests.seller-user-update-request.please-provide-a-valid-email}")
    @Email(message = "{requests.seller-user-update-request.enter-valid-email}")
    private String email;
}
