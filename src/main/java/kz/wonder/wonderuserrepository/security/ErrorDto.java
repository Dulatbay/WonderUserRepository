package kz.wonder.wonderuserrepository.security;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.slf4j.MDC;

import java.time.LocalDateTime;

import static kz.wonder.wonderuserrepository.constants.ValueConstants.ZONE_ID;

@Data
public class ErrorDto {
    @JsonProperty("error")
    private String error;

    @JsonProperty("message")
    private String message;

    @JsonProperty("stack_trace")
    private String stackTrace;

    @JsonProperty("timestamp")
    private long timestamp;

    @JsonProperty("request_id")
    private String requestId;

    public ErrorDto(String error, String message, String stackTrace) {
        this.error = error;
        this.message = message;
        this.stackTrace = stackTrace;

        this.timestamp = LocalDateTime.now(ZONE_ID).atZone(ZONE_ID).toEpochSecond();

        this.requestId = MDC.get("traceId");
    }
}
