package kz.wonder.wonderuserrepository;

import io.camunda.zeebe.spring.client.EnableZeebeClient;
import io.camunda.zeebe.spring.client.annotation.Deployment;
import kz.wonder.kaspi.client.api.KaspiApi;
import kz.wonder.wonderuserrepository.entities.BoxType;
import kz.wonder.wonderuserrepository.entities.KaspiProductCategory;
import kz.wonder.wonderuserrepository.entities.KaspiStoreAvailableTimes;
import kz.wonder.wonderuserrepository.repositories.*;
import kz.wonder.wonderuserrepository.services.AppZeebeClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@Import({KaspiApi.class})
@EnableScheduling
@Slf4j
@RequiredArgsConstructor
@EnableTransactionManagement
@EnableFeignClients(basePackages = "kz.wonder.filemanager.client.api")
@Deployment(resources = "classpath*:/bpmn-diagrams/**/*.bpmn")
public class WonderUserRepositoryApplication {


    public static void main(String[] args) {
        SpringApplication.run(WonderUserRepositoryApplication.class, args);
    }



    // todo: на некоторые сущности вместо keycloakId написать userId
}
