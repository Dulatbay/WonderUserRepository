package kz.wonder.kaspi.client.api;

import kz.wonder.kaspi.client.model.CitiesDataResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.function.Consumer;

@Component
@Slf4j
public class KaspiApi {
    @Value("${kaspi-api.token:1tOPbN07ZTNE5CO7XC+foBqdKmRKmHwr4i1Z0tkUT7c=}")
    private String token;
    private final WebClient webClient;

    public KaspiApi() {
        webClient = WebClient.builder()
                .baseUrl("https://kaspi.kz/shop/api/v2")
                .build();
    }

    private Consumer<HttpHeaders> httpHeaders() {
        return headers -> {
            headers.set("X-Auth-Token", token);
            headers.set("Accept", "*/*");
        };
    }

    public Mono<CitiesDataResponse> getDataCities() {
        return webClient.get()
                .uri("/cities")
                .headers(httpHeaders())
                .retrieve()
                .bodyToMono(CitiesDataResponse.class);
    }

}
