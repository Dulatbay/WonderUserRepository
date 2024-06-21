package kz.wonder.wonderuserrepository.services.impl;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ProcessInstanceResult;
import io.camunda.zeebe.client.impl.ZeebeObjectMapper;
import kz.wonder.wonderuserrepository.services.AppZeebeClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;

@RequiredArgsConstructor
@Service
public class AppZeebeClientImpl implements AppZeebeClient {
    private final ZeebeClient zeebeClient;

    public ProcessInstanceResult startInstance(String bpmnProcessId, Object variables) {
        return zeebeClient
                .newCreateInstanceCommand()
                .bpmnProcessId(bpmnProcessId)
                .latestVersion()
                .variables(variables)
                .withResult()
                .send()
                .join();
    }



}
