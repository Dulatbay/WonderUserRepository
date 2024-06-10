package kz.wonder.wonderuserrepository;

import kz.wonder.kaspi.client.api.KaspiApi;
import kz.wonder.wonderuserrepository.services.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@Import({KaspiApi.class})
@EnableScheduling
@Slf4j
@RequiredArgsConstructor
@EnableFeignClients(basePackages  = "kz.wonder.filemanager.client.api")
public class WonderUserRepositoryApplication {

    public static void main(String[] args) {
        SpringApplication.run(WonderUserRepositoryApplication.class, args);
    }

    // Проследить циклы в объектах
    // Не логировать большие массивы, вместо этого указать просто размер
    // Не логировать контроллеры
    // Не логировать в циклах
    //

    // todo: на некоторые сущности вместо keycloakId написать userId

    @Bean
    CommandLineRunner init(FileService fileService) {
        return args -> {
            fileService.init();
            log.info("Application Successfully Started");
        };
    }
}
