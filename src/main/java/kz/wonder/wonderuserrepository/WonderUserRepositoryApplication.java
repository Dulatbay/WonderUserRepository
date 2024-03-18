package kz.wonder.wonderuserrepository;

import kz.wonder.kaspi.client.api.KaspiApi;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@Import(KaspiApi.class)
@EnableScheduling
//@EnableFeignClients(basePackages = "kz.wonder.kaspi.client.api")
public class WonderUserRepositoryApplication {
	public static void main(String[] args) {
		SpringApplication.run(WonderUserRepositoryApplication.class, args);
	}
}
