package kz.wonder.wonderuserrepository.workers.user;

import io.camunda.zeebe.spring.client.annotation.JobWorker;
import io.camunda.zeebe.spring.client.annotation.Variable;
import io.camunda.zeebe.spring.client.exception.ZeebeBpmnError;
import kz.wonder.wonderuserrepository.mappers.UserMapper;
import kz.wonder.wonderuserrepository.repositories.KaspiTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class SellerUserWorkers {

    private final KaspiTokenRepository kaspiTokenRepository;
    private final UserMapper userMapper;

    @JobWorker(type = "getSellerUserByKeycloakId")
    public Map<String, Object> getUserByKeycloakId(@Variable("keycloakId") String keycloakId) {
        Map<String, Object> result = new HashMap<>();

        var userToken = kaspiTokenRepository.findByWonderUserKeycloakIdWithFetch(keycloakId)
                .orElseThrow(() -> new ZeebeBpmnError("403", ""));


        result.put("sellerDto", userMapper.toUserResponse(userToken));

        return result;
    }
}
