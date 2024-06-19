package kz.wonder.wonderuserrepository.services.impl;


import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ProcessInstanceResult;
import kz.wonder.wonderuserrepository.services.AppZeebeClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class AppZeebeClientImpl implements AppZeebeClient {
    private final ZeebeClient zeebeClient;

    public ProcessInstanceResult startInstance(String bpmnProcessId, Object variables) {
        return zeebeClient.newCreateInstanceCommand()
                .bpmnProcessId(bpmnProcessId)
                .latestVersion()
                .variables(variables)
                .withResult()
                .send()
                .join();
    }


}
