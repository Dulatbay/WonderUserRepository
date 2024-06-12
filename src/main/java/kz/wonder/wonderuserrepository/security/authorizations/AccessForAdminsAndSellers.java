package kz.wonder.wonderuserrepository.security.authorizations;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.*;

@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@PreAuthorize("hasAuthority(T(kz.wonder.wonderuserrepository.security.keycloak.KeycloakRole).SUPER_ADMIN) or " +
        "hasAuthority(T(kz.wonder.wonderuserrepository.security.keycloak.KeycloakRole).ADMIN) or " +
        "hasAuthority(T(kz.wonder.wonderuserrepository.security.keycloak.KeycloakRole).SELLER)")
public @interface AccessForAdminsAndSellers {
}
