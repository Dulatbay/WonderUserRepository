package kz.wonder.wonderuserrepository;

import kz.wonder.kaspi.client.api.KaspiApi;
import kz.wonder.wonderuserrepository.services.CityService;
import kz.wonder.wonderuserrepository.services.FileService;
import kz.wonder.wonderuserrepository.services.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(KaspiApi.class)
//@EnableScheduling
@Slf4j
//@EnableFeignClients(basePackages = "kz.wonder.kaspi.client.api")
public class WonderUserRepositoryApplication {
	public static void main(String[] args) {
		SpringApplication.run(WonderUserRepositoryApplication.class, args);
	}

	@Value("${application.sync-users}")
	private Boolean syncUsers;

	@Bean
	CommandLineRunner init(UserService userService,
	                       FileService fileService,
	                       CityService cityService) {
		return args -> {
			if (syncUsers)
				userService.syncUsersBetweenDBAndKeycloak();
			cityService.syncWithKaspi();
			fileService.init();
			log.info("Successfully started");
		};
	}
}
