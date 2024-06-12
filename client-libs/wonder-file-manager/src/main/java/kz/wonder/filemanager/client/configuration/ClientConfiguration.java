package kz.wonder.filemanager.client.configuration;

import feign.RequestInterceptor;
import feign.codec.Encoder;
import feign.form.FormEncoder;
import feign.form.spring.SpringFormEncoder;
import kz.wonder.filemanager.client.service.KeycloakAuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
@RequiredArgsConstructor
@EnableConfigurationProperties
@ComponentScan(basePackages = "kz.wonder.filemanager.client")
public class ClientConfiguration {

    private final KeycloakAuthService keycloakAuthService;

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            String accessToken = keycloakAuthService.getAccessToken();
            requestTemplate.header("Authorization", "Bearer " + accessToken);
        };
    }

    @Bean
    public Encoder feignFormEncoder() {
        return new SpringFormEncoder();
    }
}
