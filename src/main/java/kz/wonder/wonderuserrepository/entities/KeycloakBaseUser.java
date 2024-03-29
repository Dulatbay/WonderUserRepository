package kz.wonder.wonderuserrepository.entities;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class KeycloakBaseUser {
	private String firstName;
	private String lastName;
	private String email;
	private String phoneNumber;
	private String password;
}
