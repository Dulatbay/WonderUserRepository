package kz.wonder.wonderuserrepository;

import kz.wonder.wonderuserrepository.dto.request.BoxTypeCreateRequest;
import kz.wonder.wonderuserrepository.dto.request.SellerRegistrationRequest;
import kz.wonder.wonderuserrepository.dto.request.UserAuthRequest;
import kz.wonder.wonderuserrepository.dto.response.AuthResponse;
import kz.wonder.wonderuserrepository.security.ErrorDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
	void oneActionTest() {
		var multipartBody = BoxTypeCreateRequest
				.builder()
				.name("Box type name")
				.build();
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.MULTIPART_FORM_DATA);

		HttpEntity<BoxTypeCreateRequest> httpEntity = new HttpEntity<>(multipartBody, headers);

		List<HttpMessageConverter<?>> messageConverters = new ArrayList<HttpMessageConverter<?>>();
//Add the Jackson Message converter
		MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();

// Note: here we are making this converter to process any kind of response,
// not only application/*json, which is the default behaviour
		converter.setSupportedMediaTypes(Collections.singletonList(MediaType.ALL));
		messageConverters.add(converter);
		restTemplate.getRestTemplate().setMessageConverters(messageConverters);
		var res = restTemplate.postForEntity(
				getServerUrl() + "/box-types",
				httpEntity,
				Void.TYPE);

		assertEquals(201, res.getStatusCode().value());
	}

}
