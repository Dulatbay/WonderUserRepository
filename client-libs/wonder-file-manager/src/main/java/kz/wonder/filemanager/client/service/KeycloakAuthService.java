package kz.wonder.filemanager.client.service;

import kz.wonder.filemanager.client.dto.KeycloakTokenResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.Collections;

@Service
public class KeycloakAuthService {

    @Value("${application.keycloak-url}")
    private String authServerUrl;

    @Value("${application.realm}")
    private String realm;

    @Value("${application.client-id}")
    private String clientId;

    @Value("${application.username}")
    private String username;

    @Value("${application.password}")
    private String password;

    private String accessToken;

    private Instant tokenExpiration = Instant.now();


    public String getAccessToken() {
        if (accessToken == null || tokenExpired()) {
            refreshToken();
        }
        return accessToken;
    }

    private boolean tokenExpired() {
        return Instant.now().isAfter(tokenExpiration);
    }

    private void refreshToken() {
        String url = String.format("%s/realms/%s/protocol/openid-connect/token", authServerUrl, realm);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        HttpEntity<String> request = new HttpEntity<>("grant_type=password&client_id=" +
                clientId + "&username=" + username + "&password=" + password, headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<KeycloakTokenResponse> response = restTemplate.exchange(
                url, HttpMethod.POST, request, KeycloakTokenResponse.class);

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            this.accessToken = response.getBody().getAccessToken();
            long expiresIn = response.getBody().getExpiresIn();
            tokenExpiration = Instant.now().plusSeconds(expiresIn - 30);
        } else {
            throw new RuntimeException("Failed to retrieve access token from Keycloak");
        }
    }
}
