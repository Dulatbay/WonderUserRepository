package kz.wonder.wonderuserrepository.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class KeycloakBaseUser {
	private String firstName;
	private String lastName;
	private String email;
	private String password;
}
