package kz.wonder.wonderuserrepository;

import kz.wonder.wonderuserrepository.dto.request.SellerRegistrationRequest;
import kz.wonder.wonderuserrepository.dto.response.MessageResponse;
import kz.wonder.wonderuserrepository.services.KeycloakService;
import org.junit.jupiter.api.Test;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;

@SpringBootTest(classes = WonderUserRepositoryApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class WonderWonderUserRepositoryApplicationTests {

	@LocalServerPort
	private int port;
	@Autowired
	private TestRestTemplate restTemplate;

	@MockBean
	private KeycloakService keycloakService;

	@Test
	public void whenCreateUser_thenStatus201() {
		var randomUser = SellerRegistrationRequest
				.builder()
				.email("akhan.dulatbai2@bk.ru")
				.password("spring")
				.firstName("test")
				.lastName("test")
				.phoneNumber("test2")
				.sellerId("test2")
				.sellerName("test2")
				.sellerId("test2")
				.tokenKaspi("token2")
				.build();
		UserRepresentation mockedUserRepresentation  = mock(UserRepresentation.class);


		Mockito.when(keycloakService.createUser(randomUser))
				.thenReturn(mockedUserRepresentation);

		Mockito.when(mockedUserRepresentation.getId())
				.thenReturn("testId");

		ResponseEntity<MessageResponse> res = restTemplate.postForEntity("http://localhost:" + port + "/api/auth/registration", randomUser, MessageResponse.class);
		assertEquals(HttpStatus.CREATED.value(), res.getStatusCode().value());
	}
}
