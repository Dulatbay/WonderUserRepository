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

    private final AppZeebeClient appZeebeClient;

    private final BoxTypeImageRepository boxTypeImageRepository;
    private final BoxTypeRepository boxTypeRepository;
    private final KaspiCityRepository kaspiCityRepository;
    private final KaspiOrderProductRepository kaspiOrderProductRepository;
    private final KaspiOrderRepository kaspiOrderRepository;
    private final KaspiProductCategoryRepository kaspiProductCategoryRepository;
    private final KaspiStoreAvailableTimesRepository kaspiStoreAvailableTimesRepository;
    private final KaspiStoreRepository kaspiStoreRepository;
    private final KaspiTokenRepository kaspiTokenRepository;
    private final OrderAssembleProcessRepository orderAssembleProcessRepository;
    private final OrderAssembleRepository orderAssembleRepository;
    private final OrderPackageProcessRepository orderPackageProcessRepository;
    private final OrderPackageRepository orderPackageRepository;
    private final ProductPriceRepository productPriceRepository;
    private final ProductRepository productRepository;
    private final ProductSizeRepository productSizeRepository;
    private final StoreCellProductRepository storeCellProductRepository;
    private final StoreCellRepository storeCellRepository;
    private final StoreEmployeeRepository storeEmployeeRepository;
    private final SupplyBoxProductsRepository supplyBoxProductsRepository;
    private final SupplyBoxRepository supplyBoxRepository;
    private final SupplyRepository supplyRepository;
    private final TelegramAccountRepository telegramAccountRepository;
    private final UserRepository userRepository;


    public static void main(String[] args) {
        SpringApplication.run(WonderUserRepositoryApplication.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner() {
        return args -> {
            log.info("Starting command line runner");

            log.info("Fetching store cell products");
            storeCellProductRepository.findAll();

            log.info("Fetching supply box products");
            supplyBoxProductsRepository.findAll();

            log.info("Fetching store employees");
            storeEmployeeRepository.findAll();

            log.info("Fetching order assemble processes");
            orderAssembleProcessRepository.findAll();

            log.info("Fetching supply boxes");
            supplyBoxRepository.findAll();

            log.info("Fetching order assembles");
            orderAssembleRepository.findAll();

            log.info("Fetching orders");
            kaspiOrderRepository.findAll();

            log.info("Fetching order products");
            kaspiOrderProductRepository.findAll();

            log.info("Fetching box type images");
            boxTypeImageRepository.findAll();

            log.info("Fetching box types");
            boxTypeRepository.findAll();

            log.info("Fetching Kaspi cities");
            kaspiCityRepository.findAll();

            log.info("Fetching Kaspi product categories");
            kaspiProductCategoryRepository.findAll();

            log.info("Fetching Kaspi store available times");
            kaspiStoreAvailableTimesRepository.findAll();

            log.info("Fetching Kaspi stores");
            kaspiStoreRepository.findAll();

            log.info("Fetching Kaspi tokens");
            kaspiTokenRepository.findAll();

            log.info("Fetching order package processes");
            orderPackageProcessRepository.findAll();

            log.info("Fetching order packages");
            orderPackageRepository.findAll();

            log.info("Fetching product prices");
            productPriceRepository.findAll();

            log.info("Fetching products");
            productRepository.findAll();

            log.info("Fetching product sizes");
            productSizeRepository.findAll();

            log.info("Fetching store cells");
            storeCellRepository.findAll();

            log.info("Fetching supplies");
            supplyRepository.findAll();

            log.info("Fetching Telegram accounts");
            telegramAccountRepository.findAll();

            log.info("Fetching users");
//            userRepository.findAll();

            log.info("Finishing command line runner");
        };
    }


    // todo: на некоторые сущности вместо keycloakId написать userId
}
