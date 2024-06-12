package kz.wonder.wonderuserrepository;

import kz.wonder.kaspi.client.api.KaspiApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
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


    // todo: на некоторые сущности вместо keycloakId написать userId
}
