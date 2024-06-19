package kz.wonder.wonderuserrepository.services;
import io.camunda.zeebe.client.api.response.ProcessInstanceResult;

import java.util.Map;
import java.util.concurrent.CompletableFuture;


public interface AppZeebeClient {
    ProcessInstanceResult startInstance(String bpmnProcessId, Object variables);
}
