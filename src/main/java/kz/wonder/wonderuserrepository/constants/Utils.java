package kz.wonder.wonderuserrepository.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import static kz.wonder.wonderuserrepository.constants.ValueConstants.USER_ID_CLAIM;
import static kz.wonder.wonderuserrepository.constants.ValueConstants.ZONE_ID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public class Utils {
    private final static Random random = new Random();
    private final static Integer lengthOfBarcode = 10;

    public static String extractIdFromToken(JwtAuthenticationToken token) {
        return token.getToken().getClaim(USER_ID_CLAIM);
    }

    public static String getStringFromExcelCell(Cell vendorCodeCell) {
        if (vendorCodeCell == null) {
            return "";
        }
        return vendorCodeCell.getCellType() == CellType.NUMERIC ?
                String.valueOf((long) vendorCodeCell.getNumericCellValue()) :
                vendorCodeCell.getStringCellValue();
    }

    public static List<String> getAuthorities(Collection<GrantedAuthority> authorities) {
        return authorities
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
    }

    public static String generateRandomNumber() {
        StringBuilder randomNumber = new StringBuilder(lengthOfBarcode);
        for (int i = 0; i < lengthOfBarcode; i++) {
            randomNumber.append(random.nextInt(10));
        }
        return randomNumber.toString();
    }

    public static LocalDateTime getLocalDateTimeFromTimestamp(Long timestamp) {
        return Instant.ofEpochMilli(timestamp).atZone(ZONE_ID).toLocalDateTime();
    }
}
