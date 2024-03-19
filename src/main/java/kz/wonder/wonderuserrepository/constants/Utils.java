package kz.wonder.wonderuserrepository.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Collection;
import java.util.List;

import static kz.wonder.wonderuserrepository.constants.ValueConstants.USER_ID_CLAIM;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Utils {
    public static String extractIdFromToken(JwtAuthenticationToken token){
        return token.getToken().getClaim(USER_ID_CLAIM);
    }

    public static List<String> getAuthorities(Collection<GrantedAuthority> authorities){
        return authorities
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
    }
}
