package kz.wonder.wonderuserrepository;

import kz.wonder.wonderuserrepository.dto.request.SellerRegistrationRequest;
import kz.wonder.wonderuserrepository.dto.request.UserAuthRequest;
import kz.wonder.wonderuserrepository.dto.response.AuthResponse;
import kz.wonder.wonderuserrepository.dto.response.BoxTypeResponse;
import kz.wonder.wonderuserrepository.security.ErrorDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = WonderUserRepositoryApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class WonderWonderUserRepositoryApplicationTests {
	@LocalServerPort
	private int port;
	@Autowired
	private TestRestTemplate restTemplate;
	private String accessToken;

	private static final String SERVER_URL = "http://localhost:%d/api";
	private static final String AUTH_REGISTRATION = "/auth/registration";
	private static final String AUTH_LOGIN = "/auth/login";
	private static final String BOX_TYPES = "/box-types";

	private SellerRegistrationRequest getRandomUser() {
		return SellerRegistrationRequest
				.builder()
				.email("tester@mail.ru")
				.password("test_tester")
				.firstName("test")
				.lastName("test")
				.phoneNumber("test2")
				.sellerId("test2")
				.sellerName("test2")
				.tokenKaspi("token2")
				.build();
	}

	private static UserAuthRequest getAuthRequest() {
		return new UserAuthRequest("tester@mail.ru", "test_tester");
	}

	private HttpEntity<MultiValueMap<String, Object>> createHttpEntity(MultiValueMap<String, Object> boxData) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.MULTIPART_FORM_DATA);
		return new HttpEntity<>(boxData, headers);
	}

	private static MultiValueMap<String, Object> getBoxTypeCreateAsMultiValueMap(String name, String description) {
		MultiValueMap<String, Object> multipartRequest = new LinkedMultiValueMap<>();
		multipartRequest.add("name", name);
		multipartRequest.add("description", description);
		return multipartRequest;
	}

	private String getServerUrl() {
		return String.format(SERVER_URL, port);
	}

	@BeforeEach
	public void initUser() {
		var res = restTemplate.postForEntity(getServerUrl() + AUTH_LOGIN, getAuthRequest(), AuthResponse.class);
		accessToken = Objects.requireNonNull(res.getBody()).getAccessToken();
		restTemplate.getRestTemplate().setInterceptors(Collections.singletonList((request, body, execution) -> {
			request.getHeaders().add("Authorization", "Bearer " + accessToken);
			return execution.execute(request, body);
		}));
	}

	@Test
	public void whenCreateUser_thenStatus400() {
		ResponseEntity<ErrorDto> response = restTemplate
				.postForEntity(getServerUrl() + AUTH_REGISTRATION, getRandomUser(), ErrorDto.class);
		assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode().value());
		assertEquals("User exists with same username", Objects.requireNonNull(response.getBody()).getMessage());
	}


	@Test
	void whenGetBoxTypes_thenStatus200() {
		MultiValueMap<String, Object> firstBox = getBoxTypeCreateAsMultiValueMap("Box 1", "First Box Description");
		HttpEntity<MultiValueMap<String, Object>> httpEntity = createHttpEntity(firstBox);
		restTemplate.postForEntity(getServerUrl() + BOX_TYPES, httpEntity, Void.class);
		MultiValueMap<String, Object> secondBox = getBoxTypeCreateAsMultiValueMap("Box 2", "Second Box Description");
		httpEntity = createHttpEntity(secondBox);
		restTemplate.postForEntity(getServerUrl() + BOX_TYPES, httpEntity, Void.class);

		ResponseEntity<List<BoxTypeResponse>> response = restTemplate.exchange(
				getServerUrl() + "/box-types",
				HttpMethod.GET,
				null,
				new ParameterizedTypeReference<>() {
				});
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals(2, response.getBody().size(), "There should be exactly 2 boxes in the system.");
	}

	@Test
	void whenDeleteBoxType_thenStatus200() {
		MultiValueMap<String, Object> box = getBoxTypeCreateAsMultiValueMap("Box 3", "Third Box Description");
		HttpEntity<MultiValueMap<String, Object>> httpEntity = createHttpEntity(box);
		ResponseEntity<Void> createResponse = restTemplate.postForEntity(getServerUrl() + BOX_TYPES, httpEntity, Void.class);
		assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());

		ResponseEntity<List<BoxTypeResponse>> initialResponse = restTemplate.exchange(
				getServerUrl() + "/box-types",
				HttpMethod.GET,
				null,
				new ParameterizedTypeReference<>() {
				});
		assertEquals(HttpStatus.OK, initialResponse.getStatusCode());
		assertNotNull(initialResponse.getBody());
		int initialSize = initialResponse.getBody().size();

		ResponseEntity<Void> deleteResponse = restTemplate.exchange(
				getServerUrl() + "/box-types/" + initialResponse.getBody().get(initialSize - 1).id(),
				HttpMethod.DELETE,
				null,
				Void.class);
		assertEquals(HttpStatus.NO_CONTENT, deleteResponse.getStatusCode());

		ResponseEntity<List<BoxTypeResponse>> finalResponse = restTemplate.exchange(
				getServerUrl() + "/box-types",
				HttpMethod.GET,
				null,
				new ParameterizedTypeReference<>() {});
		assertEquals(HttpStatus.OK, finalResponse.getStatusCode());
		assertNotNull(finalResponse.getBody());
		int finalSize = finalResponse.getBody().size();

		assertEquals(initialSize - 1, finalSize, "One box should be deleted from the system.");
	}
}
