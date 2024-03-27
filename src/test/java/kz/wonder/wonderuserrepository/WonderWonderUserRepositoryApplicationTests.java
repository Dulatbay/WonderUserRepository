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

	public WonderWonderUserRepositoryApplicationTests() {
		System.out.println("constructor");
	}

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
				.sellerId("test2")
				.tokenKaspi("token2")
				.build();
	}

	private static UserAuthRequest getAuthRequest() {
		return new UserAuthRequest("tester@mail.ru", "test_tester");
	}

	private String getServerUrl() {
		return String.format("http://localhost:%d/api", port);
	}

	@Test
	public void whenCreateUser_thenStatus400() {
		ResponseEntity<ErrorDto> res = restTemplate
				.postForEntity(getServerUrl() + "/auth/registration", getRandomUser(), ErrorDto.class);

		assertEquals(HttpStatus.BAD_REQUEST.value(), res.getStatusCode().value());
		assertEquals("User exists with same username", Objects.requireNonNull(res.getBody()).getMessage());
	}


	@BeforeEach
	public void initUser() {
		var res = restTemplate.postForEntity(getServerUrl() + "/auth/login", getAuthRequest(), AuthResponse.class);
		accessToken = Objects.requireNonNull(res.getBody()).getAccessToken();
		restTemplate.getRestTemplate().setInterceptors(Collections.singletonList((request, body, execution) -> {
			request.getHeaders().add("Authorization", "Bearer " + accessToken);
			return execution.execute(request, body);
		}));
	}

	@Test
	void whenGetBoxTypes_thenStatus200() {
		// Добавляем первый бокс
		MultiValueMap<String, Object> firstBox = getBoxTypeCreateAsMultiValueMap("Box 1", "First Box Description");
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.MULTIPART_FORM_DATA);
		HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity<>(firstBox, headers);
		restTemplate.postForEntity(getServerUrl() + "/box-types", httpEntity, Void.class);

		MultiValueMap<String, Object> secondBox = getBoxTypeCreateAsMultiValueMap("Box 2", "Second Box Description");
		httpEntity = new HttpEntity<>(secondBox, headers);
		restTemplate.postForEntity(getServerUrl() + "/box-types", httpEntity, Void.class);

		ResponseEntity<List<BoxTypeResponse>> response = restTemplate.exchange(
				getServerUrl() + "/box-types",
				HttpMethod.GET,
				null,
				new ParameterizedTypeReference<>() {});

		// Проверяем статус ответа и количество боксов
		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals(2, response.getBody().size(), "There should be exactly 2 boxes in the system.");
	}


	private static MultiValueMap<String, Object> getBoxTypeCreateAsMultiValueMap(String name, String description) {
		MultiValueMap<String, Object> multipartRequest = new LinkedMultiValueMap<>();
		multipartRequest.add("name", name);
		multipartRequest.add("description", description);
		return multipartRequest;
	}
}
