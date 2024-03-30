package kz.wonder.wonderuserrepository.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import kz.wonder.wonderuserrepository.entities.KeycloakBaseUser;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
public class EmployeeCreateRequest extends KeycloakBaseUser {
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private Long storeId;

    @JsonIgnore
    private String keycloakId;
}
