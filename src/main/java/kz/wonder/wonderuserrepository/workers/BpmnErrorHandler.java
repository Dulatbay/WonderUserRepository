package kz.wonder.wonderuserrepository.workers;

import io.camunda.zeebe.spring.client.annotation.JobWorker;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class BpmnErrorHandler {
    @JobWorker(type = "errorHandler")
    public Map<String, Object> handleError() {
        return new HashMap<>();
    }
}
