package kz.wonder.kaspi.client.api;

import kz.wonder.kaspi.client.model.CitiesDataResponse;
import kz.wonder.kaspi.client.model.OrderState;
import kz.wonder.kaspi.client.model.OrdersDataResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.function.Consumer;

@Component
@Slf4j
public class KaspiApi {
    @Value("${kaspi-api.token:1tOPbN07ZTNE5CO7XC+foBqdKmRKmHwr4i1Z0tkUT7c=}")
    private String token;

    private final WebClient webClient;
    private final String kaspiUrl = "https://kaspi.kz/shop/api/v2";

    public KaspiApi() {
        webClient = WebClient.builder()
                .baseUrl(kaspiUrl)
                .build();

    }

    private Consumer<HttpHeaders> httpHeaders(String token) {
        return headers -> {
            headers.set("X-Auth-Token", token);
            headers.set("Accept", "*/*");
        };
    }

    public Mono<CitiesDataResponse> getDataCities() {
        return webClient.get()
                .uri("/cities")
                .headers(httpHeaders(token))
                .retrieve()
                .bodyToMono(CitiesDataResponse.class)
                .timeout(Duration.ofSeconds(100));
    }

    public Mono<CitiesDataResponse> getDataCitiesWithToken(String token) {
        return webClient.get()
                .uri("/cities")
                .headers(httpHeaders(token))
                .retrieve()
                .bodyToMono(CitiesDataResponse.class)
                .timeout(Duration.ofSeconds(100));
    }

    public Mono<OrdersDataResponse> getOrders(String token,
                                              long startDate,
                                              long endDate,
                                              OrderState orderState,
                                              int pageNumber,
                                              int pageSize) {

        String path = "/orders";

        String filterParams = String.format(
                "filter[orders][state]=%s&filter[orders][creationDate][$ge]=%d&filter[orders][creationDate][$le]=%d&include[orders]=entries",
                orderState.name(),
                startDate,
                endDate
        );

        String paginationParams = String.format(
                "page[number]=%d&page[size]=%d",
                pageNumber,
                pageSize
        );

        String queryParams = String.format("%s&%s", paginationParams, filterParams);

        return webClient.get()
                .uri(uriBuilder -> uriBuilder.path(path).query(queryParams).build())
                .headers(httpHeaders(token))
                .retrieve()
                .bodyToMono(OrdersDataResponse.class)
                .timeout(Duration.ofSeconds(100));
    }
}
