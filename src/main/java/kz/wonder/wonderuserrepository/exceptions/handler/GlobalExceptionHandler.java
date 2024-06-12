package kz.wonder.wonderuserrepository.exceptions.handler;

import kz.wonder.wonderuserrepository.exceptions.DbObjectNotFoundException;
import kz.wonder.wonderuserrepository.security.ErrorDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.NotFoundException;
import java.time.DateTimeException;
import java.time.format.DateTimeParseException;
import java.util.Arrays;


@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
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

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorDto> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        if (e.getRootCause() instanceof DateTimeException) {
            log.error("Date format error: ", e.getRootCause());
            var errorResponse = new ErrorDto(HttpStatus.BAD_REQUEST.toString(), e.getRootCause().getLocalizedMessage(), null);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
        log.error("Type mismatch exception: ", e);
        var errorResponse = new ErrorDto(HttpStatus.BAD_REQUEST.toString(), "Type mismatch: " + e.getMessage(), getStackTrace(e));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }


    @ExceptionHandler(DbObjectNotFoundException.class)
    public ResponseEntity<ErrorDto> handlePositionNotFoundException(DbObjectNotFoundException ex) {
        log.error("DbObjectNotFoundException exception: ", ex);
        ErrorDto errorResponse = new ErrorDto(ex.getError(), ex.getMessage(), getStackTrace(ex));
        return ResponseEntity.status(ex.getHttpStatus()).body(errorResponse);
    }

    @ExceptionHandler(DateTimeParseException.class)
    public ResponseEntity<ErrorDto> argumentExceptionHandler(DateTimeParseException e) {
        log.error("DateTimeParseException exception: ", e);
        var errorResponse = new ErrorDto(HttpStatus.BAD_REQUEST.toString(), e.getMessage(), getStackTrace(e));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
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

    @ExceptionHandler(FileUploadException.class)
    public ResponseEntity<ErrorDto> handleValidationErrors(FileUploadException ex) {
        log.error("FileUploadException exception: ", ex);
        ErrorDto errorResponse = new ErrorDto(ex.getLocalizedMessage(), ex.getMessage(), getStackTrace(ex));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorDto> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        log.error("HttpMessageNotReadableException exception: ", ex);
        ErrorDto errorResponse = new ErrorDto(HttpStatus.BAD_REQUEST.getReasonPhrase(), "Не валидное тело запроса", getStackTrace(ex));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

}
