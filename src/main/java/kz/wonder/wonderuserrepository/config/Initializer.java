package kz.wonder.wonderuserrepository.config;

import kz.wonder.wonderuserrepository.controllers.AuthController;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Random;
import java.util.UUID;


@Component
@RequiredArgsConstructor
public class Initializer {
	private final AuthController authController;
	public void init() {
//		for (int i = 0; i < 5; i++)
//			authController.registrationAsSeller(getRandomValue());
	}

	private String generateRandomEmail() {
		return "user" + UUID.randomUUID() + "@example.com";
	}

	// Generates a random name
	private String generateRandomName() {
		String[] names = {"Alex", "Jordan", "Taylor", "Morgan", "Casey"};
		return names[new Random().nextInt(names.length)];
	}

	// Generates a random alphanumeric string
	private String generateRandomString() {
		return UUID.randomUUID().toString().replaceAll("-", "");
	}

	// Generates a random phone number
	private String generateRandomPhoneNumber() {
		int num1 = (int) (Math.random() * 900 + 100);
		int num2 = (int) (Math.random() * 900 + 100);
		int num3 = (int) (Math.random() * 9000 + 1000);
		return "+7" + num1 + "-" + num2 + "-" + num3;
	}
}