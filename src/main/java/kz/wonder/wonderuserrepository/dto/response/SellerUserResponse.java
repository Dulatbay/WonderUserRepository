package kz.wonder.wonderuserrepository.dto.response;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class SellerUserResponse  {
    private Long id;
    private String phoneNumber;

    private String sellerName;
    private String sellerId;
    private String tokenKaspi;
    private String pathToXml;
    private LocalDateTime xmlUpdatedAt;

    private String firstName;
    private String lastName;
    private String email;
}
