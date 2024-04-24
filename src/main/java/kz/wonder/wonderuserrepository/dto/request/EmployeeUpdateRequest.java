package kz.wonder.wonderuserrepository.dto.request;


import kz.wonder.wonderuserrepository.dto.KeycloakBaseUser;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class EmployeeUpdateRequest extends KeycloakBaseUser {
	private String phoneNumber;
	private Long storeId;
}
