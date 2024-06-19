package kz.wonder.wonderuserrepository;

import io.camunda.zeebe.spring.client.EnableZeebeClient;
import io.camunda.zeebe.spring.client.annotation.Deployment;
import io.camunda.zeebe.spring.client.annotation.ZeebeDeployment;
import kz.wonder.kaspi.client.api.KaspiApi;
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

import java.util.HashMap;

@SpringBootApplication
@Import({KaspiApi.class})
@EnableScheduling
@Slf4j
@RequiredArgsConstructor
@EnableTransactionManagement
@EnableFeignClients(basePackages = "kz.wonder.filemanager.client.api")
@EnableZeebeClient
@Deployment(resources = "classpath*:/bpmn-diagrams/**/*.bpmn")
public class WonderUserRepositoryApplication {

    private final AppZeebeClient appZeebeClient;

    public static void main(String[] args) {
        SpringApplication.run(WonderUserRepositoryApplication.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner() {
        return args -> {
            System.out.println("WonderUserRepositoryApplication");
            var variables = appZeebeClient.startInstance("create-supply", new HashMap<>()).getVariablesAsMap();
            System.out.println("BpmnProcessId: " + variables);
        };
    }

    // todo: на некоторые сущности вместо keycloakId написать userId
}
