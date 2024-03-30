package kz.wonder.wonderuserrepository.config;


import lombok.extern.slf4j.Slf4j;
import org.jboss.resteasy.client.jaxrs.internal.ResteasyClientBuilderImpl;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class Beans {
	@Value("${application.keycloak-url}")
	private String keycloakUrl;

	@Value("${application.realm}")
	private String realm;

	@Value("${application.client-id}")
	private String clintId;

	@Value("${application.username}")
	private String username;

	@Value("${application.password}")
	private String password;

	@Bean
	Keycloak keycloak() {
		return KeycloakBuilder.builder()
				.serverUrl(keycloakUrl)
				.realm(realm)
				.clientId(clintId)
				.grantType(OAuth2Constants.PASSWORD)
				.username(username)
				.password(password)
				.resteasyClient(new ResteasyClientBuilderImpl()
						.connectionPoolSize(10).build())
				.build();
	}
}
