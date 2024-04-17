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
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@Import(KaspiApi.class)
@EnableScheduling
@Slf4j
@RequiredArgsConstructor
public class WonderUserRepositoryApplication {
	public static void main(String[] args) {
		SpringApplication.run(WonderUserRepositoryApplication.class, args);
	}

	@Value("${application.sync-users}")
	private Boolean syncUsers;

	@Value("${application.sync-cities}")
	private Boolean syncCities;

	@Value("${application.init-users}")
	private Boolean initUsers;

	private final Initializer initializer;

	@Bean
	CommandLineRunner init(UserService userService,
	                       FileService fileService,
	                       CityService cityService) {
		return args -> {
			if (syncUsers)
				userService.syncUsersBetweenDBAndKeycloak();

			if (syncCities)
				cityService.syncWithKaspi();

			if (initUsers)
				initializer.init();

			fileService.init();

			log.info("Application Successfully Started");
		};
	}
}
