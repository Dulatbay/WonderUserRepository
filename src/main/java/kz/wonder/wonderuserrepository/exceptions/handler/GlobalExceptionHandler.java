package kz.wonder.wonderuserrepository.exceptions.handler;

import kz.wonder.wonderuserrepository.exceptions.DbObjectNotFoundException;
import kz.wonder.wonderuserrepository.security.ErrorDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.NotFoundException;
import java.util.Arrays;


@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(DbObjectNotFoundException.class)
    public ResponseEntity<ErrorDto> handlePositionNotFoundException(DbObjectNotFoundException ex) {
        log.error("DbObjectNotFoundException exception: ", ex);
        ErrorDto errorResponse = new ErrorDto(ex.getError(), ex.getMessage(), getStackTrace(ex));
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorDto> argumentExceptionHandler(IllegalArgumentException e) {
        log.error("Argument exception: ", e);
        var errorResponse = new ErrorDto(HttpStatus.BAD_REQUEST.toString(), e.getMessage(), getStackTrace(e));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDto> handleValidationErrors(MethodArgumentNotValidException ex) {
        log.error("MethodArgumentNotValidException exception: ", ex);
        String error = ex.getBindingResult().getFieldErrors().getFirst().getDefaultMessage();
        ErrorDto errorResponse = new ErrorDto(HttpStatus.BAD_REQUEST.getReasonPhrase(), error, getStackTrace(ex));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(ClientErrorException.class)
    public ResponseEntity<ErrorDto> handleValidationErrors(ClientErrorException ex) {
        log.error("ClientErrorException exception: ", ex);
        ErrorDto errorResponse = new ErrorDto(ex.getLocalizedMessage(), ex.getMessage(), getStackTrace(ex));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorDto> handleValidationErrors(BadRequestException ex) {
        log.error("BadRequestException exception: ", ex);
        ErrorDto errorResponse = new ErrorDto(HttpStatus.BAD_REQUEST.getReasonPhrase(), ex.getMessage(), getStackTrace(ex));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorDto> handleValidationErrors(NotFoundException ex) {
        log.error("NotFoundException exception: ", ex);
        ErrorDto errorResponse = new ErrorDto(HttpStatus.NOT_FOUND.getReasonPhrase(), ex.getMessage(), getStackTrace(ex));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(NotAuthorizedException.class)
    public ResponseEntity<ErrorDto> handleValidationErrors(NotAuthorizedException ex) {
        log.error("NotAuthorizedException exception: ", ex);
        ErrorDto errorResponse = new ErrorDto(ex.getLocalizedMessage(), ex.getMessage(), getStackTrace(ex));
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    private static String getStackTrace(Throwable e) {
        filterStackTracesByProjectPackage(e);
        filterStackTracesByProjectPackage(e.getCause());
        return ExceptionUtils.getStackTrace(e).trim();
    }

    private static void filterStackTracesByProjectPackage(Throwable ex) {
        if (ex == null) return;

        StackTraceElement[] stackTraces = Arrays.stream(ex.getStackTrace())
                .filter(se -> se.getClassName().startsWith("com."))
                .toArray(StackTraceElement[]::new);

        ex.setStackTrace(stackTraces);
    }
}
