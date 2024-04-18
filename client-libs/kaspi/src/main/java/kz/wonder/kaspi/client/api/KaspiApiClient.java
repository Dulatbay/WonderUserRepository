package kz.wonder.kaspi.client.api;

import feign.Headers;
import kz.wonder.kaspi.client.configuration.ClientConfiguration;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@FeignClient(name = "${kaspi-api.name:kaspi}", url = "${kaspi-api.url:https://kaspi.kz/shop/api/v2}", configuration = {ClientConfiguration.class})
@Headers({"Content-Type: application/vnd.api+json", "User-Agent: Mozilla"})
public interface KaspiApiClient {
    @RequestMapping(
            method = RequestMethod.GET,
            value = "/cities",
            produces = "*/*"
    )
    ResponseEntity<Object> getCities();

}
