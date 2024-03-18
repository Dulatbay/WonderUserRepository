package kz.wonder.kaspi.client.configuration;

import feign.RequestInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
@EnableConfigurationProperties
public class ClientConfiguration {

    @Value("${kaspi.token:1tOPbN07ZTNE5CO7XC+foBqdKmRKmHwr4i1Z0tkUT7c=}")
    private String token;

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            requestTemplate.header("X-Auth-Token", token);
        };
    }


}
