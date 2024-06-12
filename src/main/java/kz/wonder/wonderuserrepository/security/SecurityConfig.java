package kz.wonder.wonderuserrepository.security;

import com.nimbusds.jose.shaded.gson.internal.LinkedTreeMap;
import kz.wonder.wonderuserrepository.security.keycloak.KeycloakRole;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.channel.ChannelProcessingFilter;

import java.util.*;
import java.util.stream.Collectors;

@Configuration
@RequiredArgsConstructor
@EnableMethodSecurity
@EnableWebSecurity
public class SecurityConfig {

    private static final String[] WHITE_LIST_URL = {
            "/v2/api-docs",
            "/v3/api-docs",
            "/v3/api-docs/**",
            "/swagger-resources",
            "/swagger-resources/**",
            "/kz/wonder/filemanager/client/configuration/ui",
            "/kz/wonder/filemanager/client/configuration/security",
            "/swagger-ui/**",
            "/webjars/**",
            "/swagger-ui.html",
            "/auth/**",
            "/actuator/**",
            "/sellers/registration",
    };
    @Value("${application.client-id}")
    private String clientId;

    @Bean
    public SecurityFilterChain configure(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(AbstractHttpConfigurer::disable)

                .oauth2ResourceServer((oauth2) -> oauth2
                        .jwt(Customizer.withDefaults())
                );

        http
                .sessionManagement(config -> config.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.exceptionHandling(httpSecurityExceptionHandlingConfigurer -> httpSecurityExceptionHandlingConfigurer.accessDeniedHandler(new CustomAccessDeniedHandler())
        );

        http.addFilterBefore(new CustomCorsFilter(), ChannelProcessingFilter.class);

        authorizeEndpoint(http, HttpMethod.POST, new String[]{"/box-types"}, KeycloakRole.ADMIN);
        authorizeEndpoint(http, HttpMethod.DELETE, new String[]{"/box-types/**"}, KeycloakRole.ADMIN);
        authorizeEndpoint(http, HttpMethod.POST, new String[]{"/stores/**"}, KeycloakRole.SUPER_ADMIN, KeycloakRole.ADMIN);
        authorizeEndpoint(http, HttpMethod.DELETE, new String[]{"/stores/**"}, KeycloakRole.SUPER_ADMIN, KeycloakRole.ADMIN);
        authorizeEndpoint(http, HttpMethod.PUT, new String[]{"/stores/**"}, KeycloakRole.SUPER_ADMIN, KeycloakRole.ADMIN);
        authorizeEndpoint(http, HttpMethod.GET, new String[]{"/cities"}, KeycloakRole.SUPER_ADMIN);
        authorizeEndpoint(http, HttpMethod.POST, new String[]{"/cities/**"}, KeycloakRole.SUPER_ADMIN);
        authorizeEndpoint(http, HttpMethod.POST, new String[]{"/employees", "/employees/**"}, KeycloakRole.SUPER_ADMIN, KeycloakRole.ADMIN);
        authorizeEndpoint(http, HttpMethod.GET, new String[]{"/employees", "/employees/**"}, KeycloakRole.SUPER_ADMIN, KeycloakRole.ADMIN);
        authorizeEndpoint(http, HttpMethod.DELETE, new String[]{"/employees", "/employees/**"}, KeycloakRole.SUPER_ADMIN, KeycloakRole.ADMIN);
        authorizeEndpoint(http, HttpMethod.PUT, new String[]{"/employees", "/employees/**"}, KeycloakRole.SUPER_ADMIN, KeycloakRole.ADMIN);
        authorizeEndpoint(http, HttpMethod.PATCH, new String[]{"/employees", "/employees/**"}, KeycloakRole.SUPER_ADMIN, KeycloakRole.ADMIN);

        http.authorizeHttpRequests(auth -> auth
                .requestMatchers(WHITE_LIST_URL)
                .permitAll()
                .anyRequest()
                .authenticated()
        );

        return http.build();
    }

    private void authorizeEndpoint(HttpSecurity http, HttpMethod method, String[] paths, KeycloakRole... roles) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers(method, paths)
                .hasAnyAuthority(Arrays.stream(roles).map(KeycloakRole::name).toArray(String[]::new))
        );
    }


    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverterForKeycloak() {
        Converter<Jwt, Collection<GrantedAuthority>> jwtGrantedAuthoritiesConverter = jwt -> {
            Map<String, Object> resourceAccess = jwt.getClaim("resource_access");
            Object client = resourceAccess.get(clientId);

            if (client == null)
                return List.of();


            @SuppressWarnings("unchecked")
            LinkedTreeMap<String, List<String>> clientRoleMap = (LinkedTreeMap<String, List<String>>) client;

            List<String> clientRoles = new ArrayList<>(clientRoleMap.get("roles"));


            return clientRoles.stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());
        };

        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();

        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);

        return jwtAuthenticationConverter;
    }
}
