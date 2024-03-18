package kz.wonder.wonderuserrepository.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import static kz.wonder.wonderuserrepository.constants.ValueConstants.USER_ID_CLAIM;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Utils {
    public static String extractIdFromToken(JwtAuthenticationToken token){
        return token.getToken().getClaim(USER_ID_CLAIM);
    }
}
