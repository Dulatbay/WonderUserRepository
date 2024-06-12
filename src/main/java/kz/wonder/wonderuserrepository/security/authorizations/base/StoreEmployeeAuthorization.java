package kz.wonder.wonderuserrepository.security.authorizations.base;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.*;

@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@PreAuthorize("hasAuthority(T(kz.wonder.wonderuserrepository.security.keycloak.KeycloakRole).STORE_EMPLOYEE)")
public @interface StoreEmployeeAuthorization { }
