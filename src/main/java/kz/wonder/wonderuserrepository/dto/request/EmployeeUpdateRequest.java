package kz.wonder.wonderuserrepository.dto.request;


import lombok.Data;

@Data
public class EmployeeUpdateRequest {
    private String phoneNumber;
    private Long storeId;
    private String firstName;
    private String lastName;
    private String email;
}
