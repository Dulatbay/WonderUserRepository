package kz.wonder.wonderuserrepository;

import kz.wonder.kaspi.client.api.KaspiApi;
import kz.wonder.wonderuserrepository.config.Initializer;
import kz.wonder.wonderuserrepository.services.CityService;
import kz.wonder.wonderuserrepository.services.FileService;
import kz.wonder.wonderuserrepository.services.UserService;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
//@EnableFeignClients(basePackages = "kz.wonder.kaspi.client.api")
public class WonderUserRepositoryApplication {
	private final Initializer initializer;
	@Value("${application.sync-users}")
	private Boolean syncUsers;

	public static void main(String[] args) {
		SpringApplication.run(WonderUserRepositoryApplication.class, args);
	}

	@Bean
	CommandLineRunner init(UserService userService,
	                       FileService fileService,
	                       CityService cityService) {
		return args -> {
			if (syncUsers)
				userService.syncUsersBetweenDBAndKeycloak();
			cityService.syncWithKaspi();
			fileService.init();
			initializer.init();
			log.info("Successfully started");
		};
	}
}
