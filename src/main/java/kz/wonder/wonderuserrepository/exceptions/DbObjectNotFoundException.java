package kz.wonder.wonderuserrepository.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
@NoArgsConstructor(force = true)
public class DbObjectNotFoundException extends RuntimeException {
    private HttpStatus httpStatus;
    private final String error;
    private final String message;
}
